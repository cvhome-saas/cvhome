package com.asrevo.cvhome.billing.commons;

/**
 * How often a price recurs. Mirrors Stripe's {@code recurring.interval}; the literals are also the {@code CHECK}
 * constraint on {@code billing.plan_price.billing_interval}, so adding one means a DDL edit.
 */
public enum BillingInterval {

    MONTH,
    YEAR

}
