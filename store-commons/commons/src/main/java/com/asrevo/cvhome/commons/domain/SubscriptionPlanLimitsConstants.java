package com.asrevo.cvhome.commons.domain;

import java.util.List;

import static com.asrevo.cvhome.commons.domain.SubscriptionPlanLimitKey.ACCOUNTS;
import static com.asrevo.cvhome.commons.domain.SubscriptionPlanLimitKey.ORDERS;
import static com.asrevo.cvhome.commons.domain.SubscriptionPlanLimitKey.PRODUCTS;
import static com.asrevo.cvhome.commons.domain.SubscriptionPlanLimitKey.STORES;
import static com.asrevo.cvhome.commons.domain.SubscriptionPlanLimitKey.VISITORS;
import static com.asrevo.cvhome.commons.domain.SubscriptionPlanLimitValue.of;

public final class SubscriptionPlanLimitsConstants {

    private static final Integer K = 1000;

    public static final SubscriptionPlanLimitList FREE_LIMITS = new SubscriptionPlanLimitList(
            List.of(of(STORES, 2), of(ORDERS, 50), of(PRODUCTS, 25), of(VISITORS, K), of(ACCOUNTS, 0)));

    public static final SubscriptionPlanLimitList LIMITED_LIMITS = new SubscriptionPlanLimitList(
            List.of(of(STORES, 2), of(ORDERS, 50), of(PRODUCTS, 25), of(VISITORS, K), of(ACCOUNTS, 0)));

    public static final SubscriptionPlanLimitList BASIC_LIMITS = new SubscriptionPlanLimitList(
            List.of(of(STORES, 5), of(ORDERS, 200), of(PRODUCTS, 100), of(VISITORS, 20 * K), of(ACCOUNTS, 3)));

    public static final SubscriptionPlanLimitList PERFORMANCE_LIMITS = new SubscriptionPlanLimitList(
            List.of(of(STORES, 10), of(ORDERS, 2 * K), of(PRODUCTS, K), of(VISITORS, 200 * K), of(ACCOUNTS, 10)));

    private SubscriptionPlanLimitsConstants() {
    }

}
