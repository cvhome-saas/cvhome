package com.asrevo.cvhome.billing.mappers;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.billing.commons.EntitlementKey;
import com.asrevo.cvhome.billing.commons.EntitlementValue;
import com.asrevo.cvhome.billing.commons.dto.EntitlementSnapshot;
import com.asrevo.cvhome.billing.commons.dto.PendingPlanChangeView;
import com.asrevo.cvhome.billing.commons.dto.SubscriptionView;
import com.asrevo.cvhome.billing.domain.PlanEntity;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;
import com.asrevo.cvhome.billing.domain.StoreSubscriptionEntity;
import com.asrevo.cvhome.billing.service.PlanCatalogService;

import lombok.RequiredArgsConstructor;

/**
 * Assembles the views of a subscription, resolving the catalog rows it points at.
 *
 * <p>
 * A subscription is stored as identifiers, so rendering one always needs the catalog. Keeping that lookup here means
 * the services do not each reimplement it, and means a store that has never had a plan renders as nulls rather than
 * failing.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class SubscriptionMappers {

    private final PlanCatalogService planCatalogService;

    public SubscriptionView toView(StoreSubscriptionEntity entity) {
        PlanEntity plan = entity.getPlanId() == null ? null
                : planCatalogService.findPlan(entity.getPlanId()).orElse(null);
        PlanPriceEntity price = entity.getPlanPriceId() == null ? null
                : planCatalogService.findPrice(entity.getPlanPriceId()).orElse(null);
        return new SubscriptionView(entity.getId(), entity.getStatus(),
                plan == null ? null : plan.getCode(),
                plan == null ? null : plan.getDisplayName(),
                entity.getPlanPriceId(),
                price == null ? null : price.amount(),
                entity.getCurrentPeriodEnd(), entity.getTrialEnd(), entity.isCancelAtPeriodEnd(),
                entity.getGraceUntil(), pendingChange(entity), entity.getStripeSubscriptionId() != null,
                entitlements(entity));
    }

    public EntitlementSnapshot toSnapshot(StoreSubscriptionEntity entity) {
        PlanEntity plan = entity.getPlanId() == null ? null
                : planCatalogService.findPlan(entity.getPlanId()).orElse(null);
        return new EntitlementSnapshot(entity.getId(), entity.getStatus(), entity.operable(),
                plan == null ? null : plan.getCode(), entity.getCurrentPeriodEnd(), entitlements(entity));
    }

    private Map<EntitlementKey, EntitlementValue> entitlements(StoreSubscriptionEntity entity) {
        // A store with no plan grants nothing, which is not the same as granting everything — an empty map is read as
        // "unlimited" by EntitlementSnapshot, so the status check has to be what gates such a store, and it is.
        return entity.getPlanId() == null ? Map.of() : planCatalogService.entitlementsOf(entity.getPlanId());
    }

    private PendingPlanChangeView pendingChange(StoreSubscriptionEntity entity) {
        if (entity.getPendingPlanPriceId() == null) {
            return null;
        }
        String code = planCatalogService.findPrice(entity.getPendingPlanPriceId())
                .flatMap(price -> planCatalogService.findPlan(price.getPlanId()))
                .map(PlanEntity::getCode)
                .orElse(null);
        return new PendingPlanChangeView(entity.getPendingPlanPriceId(), code, entity.getPendingEffectiveAt());
    }

}
