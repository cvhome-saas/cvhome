package com.asrevo.cvhome.billing.mappers;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.billing.commons.EntitlementKey;
import com.asrevo.cvhome.billing.commons.EntitlementValue;
import com.asrevo.cvhome.billing.commons.dto.PlanPriceView;
import com.asrevo.cvhome.billing.commons.dto.PlanView;
import com.asrevo.cvhome.billing.domain.PlanEntity;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;

/**
 * Catalog entities to the views clients see.
 *
 * <p>
 * Hand-written rather than generated: a plan's view is assembled from three aggregates that no single entity owns, so
 * there is nothing for a declarative mapper to map from.
 * </p>
 */
@Component
public class PlanCatalogMappers {

    public PlanPriceView toView(PlanPriceEntity entity) {
        return new PlanPriceView(entity.getId(), entity.amount(), entity.getBillingInterval(), entity.getTrialDays());
    }

    public PlanView toView(PlanEntity plan, List<PlanPriceEntity> prices,
                           Map<EntitlementKey, EntitlementValue> entitlements) {
        return new PlanView(plan.getId(), plan.getCode(), plan.getDisplayName(), plan.getDescription(),
                plan.getTier(), prices.stream().map(this::toView).toList(), entitlements);
    }

}
