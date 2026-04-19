package com.asrevo.cvhome.commons.domain;

public record SubscriptionPlanFeature(SubscriptionPlanFeatureCode code) {
    public static SubscriptionPlanFeature of(SubscriptionPlanFeatureCode code) {
        return new SubscriptionPlanFeature(code);
    }
}
