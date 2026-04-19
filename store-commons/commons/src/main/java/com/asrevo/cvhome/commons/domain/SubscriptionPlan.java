package com.asrevo.cvhome.commons.domain;

import java.time.Duration;
import java.time.temporal.TemporalAmount;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.asrevo.cvhome.commons.domain.SubscriptionPlanFeatureConstants.BASIC_FEATURE;
import static com.asrevo.cvhome.commons.domain.SubscriptionPlanFeatureConstants.FREE_FEATURE;
import static com.asrevo.cvhome.commons.domain.SubscriptionPlanFeatureConstants.LIMITED_FEATURE;
import static com.asrevo.cvhome.commons.domain.SubscriptionPlanFeatureConstants.PERFORMANCE_FEATURE;
import static com.asrevo.cvhome.commons.domain.SubscriptionPlanLimitsConstants.BASIC_LIMITS;
import static com.asrevo.cvhome.commons.domain.SubscriptionPlanLimitsConstants.FREE_LIMITS;
import static com.asrevo.cvhome.commons.domain.SubscriptionPlanLimitsConstants.LIMITED_LIMITS;
import static com.asrevo.cvhome.commons.domain.SubscriptionPlanLimitsConstants.PERFORMANCE_LIMITS;

@Getter
@AllArgsConstructor
public enum SubscriptionPlan {

    FREE(0L, Duration.ofDays(60L), FREE_LIMITS, FREE_FEATURE),
    LIMITED(500L, Duration.ofDays(0L), LIMITED_LIMITS, LIMITED_FEATURE),
    BASIC(1000L, Duration.ofDays(0L), BASIC_LIMITS, BASIC_FEATURE),
    PERFORMANCE(3000L, Duration.ofDays(0L), PERFORMANCE_LIMITS, PERFORMANCE_FEATURE);

    private final Long cost;

    private final TemporalAmount trialAmount;

    private final SubscriptionPlanLimitList limits;

    private final SubscriptionPlanFeatureList feature;

    public boolean exceeded(SubscriptionPlanLimitKey limitKey, Integer current) {
        return limits.exceeded(limitKey, current);
    }

}
