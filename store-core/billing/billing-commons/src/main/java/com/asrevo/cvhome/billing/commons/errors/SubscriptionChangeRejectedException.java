package com.asrevo.cvhome.billing.commons.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ExternalProviderException;

/**
 * The payment provider refused a subscription change — a declined card on an upgrade, most often.
 *
 * <p>
 * An answer, not a fault. The request reached Stripe and Stripe said no, so the local subscription is unchanged and
 * retrying it unchanged will be refused again; the customer needs a different card. This is why it renders 422 rather
 * than a gateway error, and why it must never share a {@code catch} with
 * {@link BillingProviderUnavailableException} — that one settles nothing, and treating it as a refusal would
 * downgrade a customer whose payment actually went through.
 * </p>
 */
public class SubscriptionChangeRejectedException extends ExternalProviderException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected SubscriptionChangeRejectedException(ErrorPayload payload, Throwable cause, String provider,
            String providerCode, int providerStatus) {
        super(payload, cause, provider, providerCode, providerStatus);
    }

    /**
     * @param provider       the provider that refused
     * @param store          the store whose subscription was being changed
     * @param targetPrice    the price that was being moved to
     * @param providerCode   the provider's own code, e.g. {@code card_declined} — diagnostic only
     * @param providerStatus the provider's HTTP status, or {@code 0} if there was no response
     * @param cause          the provider failure
     */
    public static SubscriptionChangeRejectedException of(String provider, Object store, Object targetPrice,
                                                         String providerCode, int providerStatus, Throwable cause) {
        return ExternalProviderException.of(BillingErrors.CHANGE_REJECTED, SubscriptionChangeRejectedException::new)
                .detail("Payment for the plan change on store %s was refused.", store)
                .param("store", store)
                .param("targetPrice", targetPrice)
                .provider(provider)
                .providerCode(providerCode)
                .providerStatus(providerStatus)
                .cause(cause)
                .build();
    }

}
