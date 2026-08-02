package com.asrevo.cvhome.payment.errors;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.ErrorCode;

/**
 * Error codes for the payment context.
 *
 * <p>
 * The webhook codes matter more than most: webhooks are processed asynchronously from the outbox, so these codes are
 * the only signal an operator gets about why a payment event failed. Distinguishing a bad signature from an
 * unreadable payload from a provider outage is what makes such a failure diagnosable at all.
 * </p>
 */
public enum PaymentErrors implements ErrorCode {

    /** Webhook signature did not verify against the configured secret — the payload cannot be trusted. */
    WEBHOOK_SIGNATURE_INVALID("PAYMENT.WEBHOOK.SIGNATURE_INVALID", ErrorCategory.VALIDATION),

    /** Webhook signature was valid but the event body could not be deserialized. */
    WEBHOOK_PAYLOAD_UNREADABLE("PAYMENT.WEBHOOK.PAYLOAD_UNREADABLE", ErrorCategory.VALIDATION),

    /** Webhook event carried a data object of a different type than the event type implies. */
    WEBHOOK_UNEXPECTED_OBJECT("PAYMENT.WEBHOOK.UNEXPECTED_OBJECT", ErrorCategory.VALIDATION),

    /**
     * The payment provider gave a definitive "no" — a declined card, most often. A decision, not a fault: the request
     * was well formed and reached the provider, which is why this is {@code UNPROCESSABLE} (422) rather than a gateway
     * error. Retrying it unchanged will be declined again.
     */
    INITIATE_REJECTED("PAYMENT.INITIATE.REJECTED", ErrorCategory.UNPROCESSABLE),

    /**
     * The payment provider could not be reached, or failed in a way that decided nothing — a connection failure, a rate
     * limit, a bad API key on our side. Nothing about the payment was settled, so a caller must treat the order as
     * indeterminate rather than declined. Kept apart from {@link #INITIATE_REJECTED} because the remedy differs
     * entirely: this one is ours or the provider's to fix, never the shopper's.
     */
    INITIATE_FAILED("PAYMENT.INITIATE.FAILED", ErrorCategory.REMOTE_SERVICE),

    /** No processor is registered for the requested payment type. */
    PROCESSOR_UNSUPPORTED("PAYMENT.PROCESSOR.UNSUPPORTED", ErrorCategory.UNPROCESSABLE),

    /** The store has no enabled configuration for the requested payment type. */
    CONFIGURATION_MISSING("PAYMENT.CONFIGURATION.MISSING", ErrorCategory.UNPROCESSABLE);

    private final String code;

    private final ErrorCategory category;

    PaymentErrors(String code, ErrorCategory category) {
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
