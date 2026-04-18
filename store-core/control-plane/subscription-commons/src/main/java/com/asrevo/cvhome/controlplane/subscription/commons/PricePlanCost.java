package com.asrevo.cvhome.controlplane.subscription.commons;

import com.asrevo.cvhome.commons.domain.SubscriptionPlan;

public record PricePlanCost(String currency, Long price) {

    public static final PricePlanCost FREE = new PricePlanCost("usd", 0L);

    public static PricePlanCost fromUsingFactor(String currency, SubscriptionPlan subscriptionPlan,
                                                RecurringPlan recurringPlan) {
        return new PricePlanCost(currency,
                (long) (subscriptionPlan.getCost() * recurringPlan.getTimes() * recurringPlan.getFactor()));
    }

    public static PricePlanCost from(String currency, SubscriptionPlan subscriptionPlan, RecurringPlan recurringPlan) {
        return new PricePlanCost(currency, (long) Math.ceil(subscriptionPlan.getCost() * recurringPlan.getTimes()));
    }
}
