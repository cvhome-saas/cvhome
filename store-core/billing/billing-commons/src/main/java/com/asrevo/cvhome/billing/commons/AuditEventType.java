package com.asrevo.cvhome.billing.commons;

/**
 * What happened to a subscription. The literals are the {@code CHECK} constraint on
 * {@code billing.subscription_audit.event_type}, so a new one means a DDL edit in the same change.
 *
 * <p>
 * Finer-grained than {@link SubscriptionStatus} on purpose: several of these change no status at all
 * ({@link #CANCEL_SCHEDULED}, {@link #PLAN_DOWNGRADE_SCHEDULED}, {@link #INVOICE_RECORDED}) and those are exactly the
 * events a billing dispute turns on.
 * </p>
 */
public enum AuditEventType {

    CREATED,
    TRIAL_STARTED,
    ACTIVATED,
    RENEWED,
    PAYMENT_FAILED,
    PAST_DUE,
    SUSPENDED,
    RESUMED,
    PLAN_UPGRADED,
    PLAN_DOWNGRADE_SCHEDULED,
    PLAN_DOWNGRADE_APPLIED,
    CANCEL_SCHEDULED,
    CANCEL_REVOKED,
    CANCELED,
    INVOICE_RECORDED,
    QUOTA_REFUSED

}
