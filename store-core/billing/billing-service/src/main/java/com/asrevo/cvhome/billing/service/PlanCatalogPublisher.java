package com.asrevo.cvhome.billing.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.billing.commons.errors.BillingProviderUnavailableException;
import com.asrevo.cvhome.billing.domain.PlanEntity;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;
import com.asrevo.cvhome.billing.repository.PlanPriceRepository;
import com.asrevo.cvhome.billing.repository.PlanRepository;
import com.asrevo.cvhome.billing.service.stripe.StripeCatalogGateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Publishes catalog rows that have no Stripe counterpart yet, so they become purchasable.
 *
 * <p>
 * Runs after the seeder, on start-up, and only for rows whose Stripe id is still null — so it converges rather than
 * republishing. A plan cannot be bought until this has run, which is why {@code checkout} reports an unpublished
 * price as "not purchasable" rather than as a provider failure.
 * </p>
 *
 * <p>
 * Failures are logged and the boot continues. Everything that does not involve taking money — the catalog, existing
 * subscriptions, entitlement reads — works without Stripe, and refusing to start would take those down too whenever
 * Stripe was unreachable.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "com.asrevo.cvhome.billing.catalog", name = "stripe-sync-enabled",
        havingValue = "true")
public class PlanCatalogPublisher {

    private final PlanRepository planRepository;

    private final PlanPriceRepository planPriceRepository;

    private final StripeCatalogGateway catalogGateway;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void publish() {
        int published = 0;
        for (PlanEntity plan : planRepository.findAllByActiveTrueOrderByTierAsc()) {
            try {
                published += publishPlan(plan);
            } catch (BillingProviderUnavailableException e) {
                log.error("Could not publish plan {} to Stripe; it stays unpurchasable until the next start",
                        plan.getCode(), e);
                return;
            }
        }
        log.info("Plan catalog published to Stripe: {} objects created", published);
    }

    private int publishPlan(PlanEntity plan) throws BillingProviderUnavailableException {
        int created = 0;
        PlanEntity current = plan;
        if (current.getStripeProductId() == null) {
            current = planRepository.save(current.publishedAs(catalogGateway.createProduct(current)));
            created++;
        }
        for (PlanPriceEntity price : planPriceRepository.findAllByPlanIdAndActiveTrue(current.getId())) {
            if (price.getStripePriceId() == null) {
                planPriceRepository.save(price.publishedAs(catalogGateway.createPrice(current, price)));
                created++;
            }
        }
        return created;
    }

}
