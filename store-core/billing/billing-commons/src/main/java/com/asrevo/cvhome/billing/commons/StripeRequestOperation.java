package com.asrevo.cvhome.billing.commons;

/**
 * The outbound Stripe mutations that carry an idempotency key. The literals are the {@code CHECK} constraint on
 * {@code billing.stripe_request.operation}.
 *
 * <p>
 * Only mutations appear here. Reads need no key, and giving them one would be misleading — the table records an
 * intent that must not be duplicated, not a log of traffic.
 * </p>
 */
public enum StripeRequestOperation {

    CUSTOMER_CREATE,
    CHECKOUT_SESSION_CREATE,
    SUBSCRIPTION_UPDATE,
    SUBSCRIPTION_CANCEL,
    SUBSCRIPTION_RESUME,
    SCHEDULE_CREATE,
    SCHEDULE_RELEASE,
    PRODUCT_CREATE,
    PRICE_CREATE

}
