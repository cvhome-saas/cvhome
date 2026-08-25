package com.asrevo.cvhome.billing.commons.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ExternalProviderException;

/**
 * A call to the payment provider settled nothing — no connection, a rate limit, a malformed request of ours, an API
 * key it rejected.
 *
 * <p>
 * The critical property is that the outcome is <em>unknown</em>: the change may have landed at the provider even
 * though we never saw the answer. So a caller must not write a local plan change on the strength of this, and must
 * not report it to the customer as a decline. Leave the row alone and let the webhook, or the reconciliation job,
 * settle what actually happened.
 * </p>
 */
public class BillingProviderUnavailableException extends ExternalProviderException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected BillingProviderUnavailableException(ErrorPayload payload, Throwable cause, String provider,
            String providerCode, int providerStatus) {
        super(payload, cause, provider, providerCode, providerStatus);
    }

    /**
     * @param provider       the provider that failed
     * @param store          the store the call was for, or {@code null} for a catalog-level call
     * @param operation      what was being attempted
     * @param providerCode   the provider's own code — diagnostic only
     * @param providerStatus the provider's HTTP status, or {@code 0} if the call never produced a response
     * @param cause          the provider failure
     */
    public static BillingProviderUnavailableException of(String provider, Object store, Object operation,
                                                         String providerCode, int providerStatus, Throwable cause) {
        return ExternalProviderException.of(BillingErrors.PROVIDER_UNAVAILABLE,
                BillingProviderUnavailableException::new)
                .detail("The %s call %s did not complete.", provider, operation)
                .param("store", store)
                .param("operation", operation)
                .provider(provider)
                .providerCode(providerCode)
                .providerStatus(providerStatus)
                .cause(cause)
                .build();
    }

}
