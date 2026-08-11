package com.asrevo.cvhome.billing.service.impl;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.billing.commons.EntitlementKey;
import com.asrevo.cvhome.billing.commons.EntitlementValue;
import com.asrevo.cvhome.billing.commons.PlanId;
import com.asrevo.cvhome.billing.commons.PlanPriceId;
import com.asrevo.cvhome.billing.commons.StripePriceId;
import com.asrevo.cvhome.billing.commons.dto.PlanView;
import com.asrevo.cvhome.billing.commons.errors.PlanNotFoundException;
import com.asrevo.cvhome.billing.commons.errors.PlanPriceNotFoundException;
import com.asrevo.cvhome.billing.domain.PlanEntity;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;
import com.asrevo.cvhome.billing.mappers.PlanCatalogMappers;
import com.asrevo.cvhome.billing.repository.PlanEntitlementRepository;
import com.asrevo.cvhome.billing.repository.PlanPriceRepository;
import com.asrevo.cvhome.billing.repository.PlanRepository;
import com.asrevo.cvhome.billing.service.PlanCatalogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanCatalogServiceImpl implements PlanCatalogService {

    private final PlanRepository planRepository;

    private final PlanPriceRepository planPriceRepository;

    private final PlanEntitlementRepository planEntitlementRepository;

    private final PlanCatalogMappers mappers;

    @Override
    @Transactional(readOnly = true)
    public List<PlanView> listActivePlans(String currency) {
        return planRepository.findAllByActiveTrueOrderByTierAsc()
                .stream()
                .map(plan -> mappers.toView(plan, pricesOf(plan, currency), entitlementsOf(plan.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PlanEntity requirePlanByCode(String code) throws PlanNotFoundException {
        return planRepository.findByCode(code)
                .filter(PlanEntity::isActive)
                .orElseThrow(() -> PlanNotFoundException.byCode(code));
    }

    @Override
    @Transactional(readOnly = true)
    public PlanEntity requirePlan(PlanId planId) throws PlanNotFoundException {
        return planRepository.findById(planId).orElseThrow(() -> PlanNotFoundException.byId(planId));
    }

    @Override
    @Transactional(readOnly = true)
    public PlanPriceEntity requirePurchasablePrice(PlanPriceId planPriceId) throws PlanPriceNotFoundException {
        return planPriceRepository.findById(planPriceId)
                .filter(PlanPriceEntity::isActive)
                .orElseThrow(() -> PlanPriceNotFoundException.of(planPriceId));
    }

    @Override
    @Transactional(readOnly = true)
    public PlanPriceEntity requirePrice(PlanPriceId planPriceId) throws PlanPriceNotFoundException {
        return planPriceRepository.findById(planPriceId)
                .orElseThrow(() -> PlanPriceNotFoundException.of(planPriceId));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<EntitlementKey, EntitlementValue> entitlementsOf(PlanId planId) {
        Map<EntitlementKey, EntitlementValue> byKey = new EnumMap<>(EntitlementKey.class);
        planEntitlementRepository.findAllByPlanId(planId)
                .forEach(it -> byKey.put(it.getEntitlementKey(), it.value()));
        return byKey;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PlanEntity> findPlan(PlanId planId) {
        return planRepository.findById(planId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PlanPriceEntity> findPrice(PlanPriceId planPriceId) {
        return planPriceRepository.findById(planPriceId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PlanPriceEntity> findByStripePriceId(String stripePriceId) {
        return stripePriceId == null
                ? Optional.empty()
                : planPriceRepository.findByStripePriceId(new StripePriceId(stripePriceId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PlanPriceEntity> cheapestActivePrice() {
        return planRepository.findAllByActiveTrueOrderByTierAsc()
                .stream()
                .map(plan -> planPriceRepository.findAllByPlanIdAndActiveTrue(plan.getId()))
                .flatMap(List::stream)
                .min(Comparator.comparing(PlanPriceEntity::getUnitAmount)
                        .thenComparing(PlanPriceEntity::getBillingInterval));
    }

    /**
     * Active prices of a plan, narrowed to one currency when the caller asked for one. Sorted so a pricing page can
     * render them without deciding an order of its own.
     */
    private List<PlanPriceEntity> pricesOf(PlanEntity plan, String currency) {
        return planPriceRepository.findAllByPlanIdAndActiveTrue(plan.getId())
                .stream()
                .filter(it -> currency == null || currency.equalsIgnoreCase(it.getCurrency().code()))
                .sorted(Comparator.comparing(PlanPriceEntity::getBillingInterval)
                        .thenComparing(PlanPriceEntity::getUnitAmount))
                .toList();
    }

}
