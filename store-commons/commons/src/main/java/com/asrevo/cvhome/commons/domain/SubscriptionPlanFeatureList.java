package com.asrevo.cvhome.commons.domain;

import java.util.List;

public record SubscriptionPlanFeatureList(List<SubscriptionPlanFeature> features) {
    public static SubscriptionPlanFeatureList of(List<SubscriptionPlanFeature> features) {
        return new SubscriptionPlanFeatureList(features);
    }
}
