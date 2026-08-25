package com.asrevo.cvhome.payment.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ExternalProviderException;

/**
 * The payment provider could not be reached, or failed in a way that settled nothing — a connection failure, a read
 * timeout, a rate limit, an authentication failure caused by our own misconfigured API key.
 *
 * <p>
 * The critical difference from {@link PaymentInitiateRejectedException}: <em>nothing was decided</em>. The payment may
 * have been started at the provider, or not, and this service cannot tell. Reporting it as a rejection would cancel
 * orders that were in fact charged, which is the mistake a single provider exception invites.
 * </p>
 *
 * <p>
 * Renders as 502 rather than 422, and that difference is the point: the shopper did nothing wrong, so nothing they can
 * change will help. A bad API key is the sharpest case — Stripe answers 401, but the party who failed to authenticate
 * is us, and {@link ExternalProviderException} is what stops that 401 being handed to the shopper as their own.
 * </p>
 */
public class PaymentProviderUnavailableException extends ExternalProviderException {

    private static final String PROVIDER = "provider";

    @Serial
    private static final long serialVersionUID = 1L;

    protected PaymentProviderUnavailableException(ErrorPayload payload, Throwable cause, String provider,
            String providerCode, int providerStatus) {
        super(payload, cause, provider, providerCode, providerStatus);
    }

    /**
     * @param provider          payment provider that could not complete the call, e.g. {@code stripe}
     * @param orderRef          the merchant-facing order reference the payment was for
     * @param internalReference this service's transaction reference, the join key to the transaction row
     * @param providerCode      the provider's own error code, or {@code null} if it sent none
     * @param providerStatus    the provider's HTTP status, or {@code 0} if the call produced no response
     */
    public static PaymentProviderUnavailableException of(String provider, String orderRef, String internalReference,
            String providerCode, int providerStatus, Throwable cause) {
        return ExternalProviderException.of(PaymentErrors.INITIATE_FAILED, PaymentProviderUnavailableException::new)
                .detail("%s did not complete the payment for order %s.", provider, orderRef)
                .param(PROVIDER, provider)
                .param("orderRef", orderRef)
                .param("internalReference", internalReference)
                .provider(provider)
                .providerCode(providerCode)
                .providerStatus(providerStatus)
                .cause(cause)
                .build();
    }

}
