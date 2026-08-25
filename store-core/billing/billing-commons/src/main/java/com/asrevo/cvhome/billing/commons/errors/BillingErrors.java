package com.asrevo.cvhome.billing.commons.errors;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.ErrorCode;

/**
 * Error codes for the billing context.
 *
 * <p>
 * The split that matters most here is {@link #CHANGE_REJECTED} against {@link #PROVIDER_UNAVAILABLE}. Money makes the
 * difference expensive: a refusal means the card said no and the subscription is unchanged, while an unavailable
 * provider means nothing was decided and the change may or may not have landed. Collapsing them would let a timeout
 * read as a decline and downgrade a customer who actually paid.
 * </p>
 */
public enum BillingErrors implements ErrorCode {

    /** No subscription row exists for the addressed store. */
    SUBSCRIPTION_NOT_FOUND("BILLING.SUBSCRIPTION.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /** The addressed plan is not in the catalog, or is no longer active. */
    PLAN_NOT_FOUND("BILLING.PLAN.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /** The addressed price is not in the catalog, or is no longer purchasable. */
    PLAN_PRICE_NOT_FOUND("BILLING.PLAN_PRICE.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /** A subscription already exists for this store. */
    SUBSCRIPTION_DUPLICATE("BILLING.SUBSCRIPTION.DUPLICATE", ErrorCategory.CONFLICT),

    /**
     * The requested move is not legal from the subscription's current state — resuming something that was never
     * scheduled to cancel, upgrading a canceled subscription.
     */
    SUBSCRIPTION_TRANSITION_ILLEGAL("BILLING.SUBSCRIPTION.TRANSITION_ILLEGAL", ErrorCategory.UNPROCESSABLE),

    /** The target plan's ceilings are already exceeded by what the store holds today. */
    DOWNGRADE_NOT_ALLOWED("BILLING.PLAN.DOWNGRADE_NOT_ALLOWED", ErrorCategory.UNPROCESSABLE),

    /** The org may not create another store right now. */
    STORE_QUOTA_EXCEEDED("BILLING.QUOTA.STORE_EXCEEDED", ErrorCategory.UNPROCESSABLE),

    /** An action would take the store past a ceiling its plan grants. */
    ENTITLEMENT_EXCEEDED("BILLING.ENTITLEMENT.EXCEEDED", ErrorCategory.UNPROCESSABLE),

    /** Cancelling immediately, rather than at period end, is reserved for platform administrators. */
    IMMEDIATE_CANCEL_FORBIDDEN("BILLING.SUBSCRIPTION.IMMEDIATE_CANCEL_FORBIDDEN", ErrorCategory.FORBIDDEN),

    /**
     * The provider gave a definitive no — a declined card on an upgrade, most often. A decision, not a fault, which is
     * why it is 422 and not a gateway error: the request reached Stripe and was answered. Retrying it unchanged will be
     * declined again, and the local subscription is untouched.
     */
    CHANGE_REJECTED("BILLING.PROVIDER.CHANGE_REJECTED", ErrorCategory.UNPROCESSABLE),

    /**
     * The provider could not be reached, or failed in a way that settled nothing — a connection failure, a rate limit, a
     * bad API key of ours. The caller must treat the change as indeterminate rather than refused, and must not write a
     * local plan change on the strength of it.
     */
    PROVIDER_UNAVAILABLE("BILLING.PROVIDER.UNAVAILABLE", ErrorCategory.REMOTE_SERVICE),

    /** Webhook signature did not verify against the configured secret — the payload cannot be trusted. */
    WEBHOOK_SIGNATURE_INVALID("BILLING.WEBHOOK.SIGNATURE_INVALID", ErrorCategory.VALIDATION),

    /** Webhook signature was valid but the event body could not be deserialized. */
    WEBHOOK_PAYLOAD_UNREADABLE("BILLING.WEBHOOK.PAYLOAD_UNREADABLE", ErrorCategory.VALIDATION),

    /** Webhook event carried a data object of a different type than its event type implies. */
    WEBHOOK_UNEXPECTED_OBJECT("BILLING.WEBHOOK.UNEXPECTED_OBJECT", ErrorCategory.VALIDATION);

    private final String code;

    private final ErrorCategory category;

    BillingErrors(String code, ErrorCategory category) {
        this.code = code;
        this.category = category;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public ErrorCategory category() {
        return category;
    }

}
