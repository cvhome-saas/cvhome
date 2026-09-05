package com.asrevo.cvhome.commons.domain;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The plan catalog, and the invariant that keeps a plan from being silently unlimited.
 *
 * <p>
 * {@link SubscriptionPlanLimitList}'s compact constructor refuses a list that does not name every
 * {@link SubscriptionPlanLimitKey}, because {@link SubscriptionPlanLimitList#exceeded} answers {@code false} for a
 * key it cannot find. A plan that forgot one limit would therefore enforce nothing on it, and the omission would
 * only surface as a store quietly exceeding a quota it was sold.
 * </p>
 */
class SubscriptionPlanTest {

    @ParameterizedTest
    @EnumSource(SubscriptionPlan.class)
    void everyPlanNamesEveryLimitKey(SubscriptionPlan plan) {
        assertThat(plan.getLimits().limits())
                .extracting(SubscriptionPlanLimitValue::limitKey)
                .containsExactlyInAnyOrder(SubscriptionPlanLimitKey.values());
    }

    @Test
    void aLimitListMissingAKeyIsRefusedAtConstruction() {
        List<SubscriptionPlanLimitValue> incomplete =
                List.of(SubscriptionPlanLimitValue.of(SubscriptionPlanLimitKey.STORES, 1));
        assertThatThrownBy(() -> new SubscriptionPlanLimitList(incomplete))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match all limits");
    }

    @Test
    void aLimitIsExceededOnlyStrictlyAboveIt() {
        SubscriptionPlanLimitList limits = SubscriptionPlan.FREE.getLimits();
        int stores = limits.limits().stream()
                .filter(it -> it.limitKey() == SubscriptionPlanLimitKey.STORES)
                .findFirst().orElseThrow().limit();
        assertThat(limits.exceeded(SubscriptionPlanLimitKey.STORES, stores)).isFalse();
        assertThat(limits.exceeded(SubscriptionPlanLimitKey.STORES, stores + 1)).isTrue();
        assertThat(limits.exceeded(SubscriptionPlanLimitKey.STORES, stores - 1)).isFalse();
    }

    @Test
    void aPlanDelegatesItsQuotaQuestionToItsLimitList() {
        assertThat(SubscriptionPlan.FREE.exceeded(SubscriptionPlanLimitKey.STORES, Integer.MAX_VALUE)).isTrue();
        assertThat(SubscriptionPlan.PERFORMANCE.exceeded(SubscriptionPlanLimitKey.STORES, 0)).isFalse();
    }

    @Test
    void onlyTheFreePlanCarriesATrialAndOnlyTheFreePlanIsFree() {
        assertThat(SubscriptionPlan.FREE.getCost()).isZero();
        assertThat(SubscriptionPlan.FREE.getTrialAmount()).isEqualTo(Duration.ofDays(60L));
        assertThat(SubscriptionPlan.LIMITED.getCost()).isPositive();
        assertThat(SubscriptionPlan.LIMITED.getTrialAmount()).isEqualTo(Duration.ZERO);
    }

    @Test
    void plansGetMoreExpensiveAsTheyGetBigger() {
        assertThat(SubscriptionPlan.FREE.getCost())
                .isLessThan(SubscriptionPlan.LIMITED.getCost());
        assertThat(SubscriptionPlan.LIMITED.getCost())
                .isLessThan(SubscriptionPlan.BASIC.getCost());
        assertThat(SubscriptionPlan.BASIC.getCost())
                .isLessThan(SubscriptionPlan.PERFORMANCE.getCost());
    }

    @ParameterizedTest
    @EnumSource(SubscriptionPlan.class)
    void everyPlanCarriesItsFeatureList(SubscriptionPlan plan) {
        assertThat(plan.getFeature()).isNotNull();
        assertThat(plan.getFeature().features()).isNotEmpty();
    }
}
