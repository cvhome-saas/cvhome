package com.asrevo.cvhome.billing.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.billing.commons.EntitlementKey;
import com.asrevo.cvhome.billing.commons.EntitlementValue;
import com.asrevo.cvhome.billing.commons.PlanId;
import com.asrevo.cvhome.billing.config.PlanCatalogProperties;
import com.asrevo.cvhome.billing.domain.PlanEntitlementEntity;
import com.asrevo.cvhome.billing.domain.PlanEntity;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;
import com.asrevo.cvhome.billing.repository.PlanEntitlementRepository;
import com.asrevo.cvhome.billing.repository.PlanPriceRepository;
import com.asrevo.cvhome.billing.repository.PlanRepository;
import com.asrevo.cvhome.commons.domain.CurrencyCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Reconciles the database against the plan catalog declared in {@code plan-catalog.yml} at start-up.
 *
 * <p>
 * Idempotent by construction: it matches on the natural keys — a plan by {@code code}, a price by
 * {@code (plan, currency, interval)} — so running it on every boot converges rather than accumulating.
 * </p>
 *
 * <p>
 * Two behaviours are deliberate and easy to mistake for bugs. A price whose amount changed is <em>not</em> edited: the
 * old row is deactivated and a new one minted, because Stripe prices are immutable and because existing subscribers
 * are owed the terms they agreed to. And a plan that disappears from the file is deactivated rather than deleted,
 * because subscriptions and invoices still point at it.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "com.asrevo.cvhome.billing.catalog", name = "seed-enabled", havingValue = "true")
public class PlanCatalogSeeder implements ApplicationRunner {

    private final PlanCatalogProperties properties;

    private final PlanRepository planRepository;

    private final PlanPriceRepository planPriceRepository;

    private final PlanEntitlementRepository planEntitlementRepository;

    /**
     * Reads a declared entitlement. The values arrive as strings because one map holds both shapes: a numeric ceiling
     * and a capability flag. An unparseable value is dropped with a warning rather than failing the boot — a typo in
     * one line of the catalog should not take billing offline.
     */
    private static EntitlementValue parse(EntitlementKey key, String raw) {
        if (raw == null || raw.isBlank()) {
            return EntitlementValue.unlimited(key);
        }
        String value = raw.trim();
        if (key.numeric()) {
            try {
                return EntitlementValue.limit(key, Integer.valueOf(value));
            } catch (NumberFormatException e) {
                log.warn("Entitlement {} needs a number, got '{}' — treating it as unlimited", key, value);
                return EntitlementValue.unlimited(key);
            }
        }
        return EntitlementValue.flag(key, Boolean.parseBoolean(value));
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<PlanCatalogProperties.Plan> declared = properties.plans();
        if (declared.isEmpty()) {
            log.warn("Plan catalog seeding is on but no plans are declared — leaving the catalog untouched");
            return;
        }
        Set<String> declaredCodes = new HashSet<>();
        for (PlanCatalogProperties.Plan plan : declared) {
            declaredCodes.add(plan.code());
            PlanEntity entity = seedPlan(plan);
            seedPrices(plan, entity.getId());
            seedEntitlements(plan, entity.getId());
        }
        withdrawUndeclaredPlans(declaredCodes);
        log.info("Plan catalog seeded: {} plans declared", declared.size());
    }

    private PlanEntity seedPlan(PlanCatalogProperties.Plan plan) {
        PlanEntity entity = planRepository.findByCode(plan.code())
                .map(it -> it.describe(plan.displayName(), plan.description(), plan.tier()))
                .orElseGet(() -> PlanEntity.create(plan.code(), plan.displayName(), plan.description(), plan.tier()));
        return planRepository.save(entity);
    }

    private void seedPrices(PlanCatalogProperties.Plan plan, PlanId planId) {
        List<PlanPriceEntity> existing = planPriceRepository.findAllByPlanId(planId);
        Set<String> declared = new HashSet<>();
        for (PlanCatalogProperties.Price price : plan.prices()) {
            CurrencyCode currency = new CurrencyCode(price.currency());
            declared.add(naturalKey(currency, price.interval().name()));
            PlanPriceEntity current = existing.stream()
                    .filter(PlanPriceEntity::isActive)
                    .filter(it -> it.getCurrency().code().equalsIgnoreCase(price.currency()))
                    .filter(it -> it.getBillingInterval() == price.interval())
                    .findFirst()
                    .orElse(null);
            if (current == null) {
                planPriceRepository.save(PlanPriceEntity.create(planId, currency, price.amount(), price.interval(),
                        price.trialDays()));
            } else if (!Objects.equals(current.getUnitAmount(), price.amount())) {
                // Not an edit. The old price stays, deactivated, so the subscribers on it keep their terms.
                planPriceRepository.save(current.deactivate());
                planPriceRepository.save(PlanPriceEntity.create(planId, currency, price.amount(), price.interval(),
                        price.trialDays()));
                log.info("Plan {} price {} {} changed to {} — minted a new price, withdrew the old one", plan.code(),
                        price.currency(), price.interval(), price.amount());
            }
        }
        existing.stream()
                .filter(PlanPriceEntity::isActive)
                .filter(it -> !declared.contains(naturalKey(it.getCurrency(), it.getBillingInterval().name())))
                .forEach(it -> planPriceRepository.save(it.deactivate()));
    }

    private void seedEntitlements(PlanCatalogProperties.Plan plan, PlanId planId) {
        Map<EntitlementKey, PlanEntitlementEntity> existing = new java.util.EnumMap<>(EntitlementKey.class);
        planEntitlementRepository.findAllByPlanId(planId).forEach(it -> existing.put(it.getEntitlementKey(), it));
        for (Map.Entry<EntitlementKey, String> entry : plan.entitlements().entrySet()) {
            EntitlementValue value = parse(entry.getKey(), entry.getValue());
            PlanEntitlementEntity entity = existing.remove(entry.getKey());
            planEntitlementRepository.save(entity == null
                    ? PlanEntitlementEntity.create(planId, value)
                    : entity.grant(value));
        }
        // A key dropped from the file becomes unlimited again, which is what its absence means everywhere else.
        existing.values().forEach(planEntitlementRepository::delete);
    }

    private void withdrawUndeclaredPlans(Set<String> declaredCodes) {
        planRepository.findAllByActiveTrueOrderByTierAsc()
                .stream()
                .filter(it -> !declaredCodes.contains(it.getCode()))
                .forEach(it -> {
                    log.info("Plan {} is no longer declared — withdrawing it from sale", it.getCode());
                    planRepository.save(it.deactivate());
                });
    }

    private String naturalKey(CurrencyCode currency, String interval) {
        return String.format("%s|%s", currency.code().toUpperCase(java.util.Locale.ROOT), interval);
    }

}
