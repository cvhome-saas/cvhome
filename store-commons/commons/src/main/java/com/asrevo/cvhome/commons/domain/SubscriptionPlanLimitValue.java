package com.asrevo.cvhome.commons.domain;

public record SubscriptionPlanLimitValue(SubscriptionPlanLimitKey limitKey, Integer limit) {
    public static SubscriptionPlanLimitValue of(SubscriptionPlanLimitKey key, Integer limit) {
        return new SubscriptionPlanLimitValue(key, limit);
    }
}
