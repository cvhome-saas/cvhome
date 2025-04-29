package com.asrevo.cvhome.subscription.commons;

import com.asrevo.cvhome.commons.domain.SubscriptionPlan;
import com.asrevo.cvhome.commons.domain.SubscriptionPlanFeatureList;
import com.asrevo.cvhome.commons.domain.SubscriptionPlanLimitList;

public record SubscriptionPlanOption(
        PriceId id,
        ProductId productId,
        PricePlanCost cost,
        PricePlanCost previousCost,
        SubscriptionPlan subscriptionPlan,
        SubscriptionPlanLimitList limits,
        SubscriptionPlanFeatureList feature,
        RecurringPlan recurringPlan) {
    public static final SubscriptionPlanOption FREE_OPTION =
            new SubscriptionPlanOption(
                    null,
                    null,
                    PricePlanCost.FREE,
                    PricePlanCost.FREE,
                    SubscriptionPlan.FREE,
                    SubscriptionPlan.FREE.getLimits(),
                    SubscriptionPlan.FREE.getFeature(),
                    null);
}
