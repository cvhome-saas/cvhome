package com.asrevo.cvhome.payment.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ExternalProviderException;

/**
 * The payment provider refused the request to start a payment — a declined card, or any other definitive "no".
 *
 * <p>
 * Extends {@link ExternalProviderException} because the system that refused is a third party, not a cvhome service. The
 * provider's own code and status survive into the logs and the response extensions, so "Stripe said
 * {@code card_declined}" stays legible, but they never become this service's code or status: the shopper's request was
 * well formed and reached us, so this answers 422, and the {@code code} on the wire stays
 * {@code PAYMENT.INITIATE.REJECTED} — which is what a caller's {@code PaymentApiErrors.CATALOG} matches on.
 * </p>
 *
 * <p>
 * Deliberately has no {@code from(RemoteErrorContext)}: a caller of the payment service did not call Stripe and has no
 * business holding an exception that claims it did. Callers receive
 * {@code com.asrevo.cvhome.payment.api.errors.PaymentGatewayRejectedException} instead, which says the true thing from
 * their position — payment-service refused them.
 * </p>
 *
 * <p>
 * Distinct from {@link PaymentProviderUnavailableException} in the only way a caller can act on: this one decided
 * something.
 * </p>
 */
public class PaymentInitiateRejectedException extends ExternalProviderException {

    /**
     * Param naming the gateway that declined, so it survives the hop into a caller's logs.
     */
    private static final String PROVIDER = "provider";

    @Serial
    private static final long serialVersionUID = 1L;

    protected PaymentInitiateRejectedException(ErrorPayload payload, Throwable cause, String provider,
            String providerCode, int providerStatus) {
        super(payload, cause, provider, providerCode, providerStatus);
    }

    /**
     * @param provider          payment provider that rejected the call, e.g. {@code stripe}
     * @param orderRef          the merchant-facing order reference the payment was for
     * @param internalReference this service's transaction reference, the join key to the transaction row
     * @param providerCode      the provider's own error code, or {@code null} if it sent none
     * @param providerStatus    the provider's HTTP status, or {@code 0} if the call produced no response
     */
    public static PaymentInitiateRejectedException of(String provider, String orderRef, String internalReference,
            String providerCode, int providerStatus, Throwable cause) {
        return ExternalProviderException.of(PaymentErrors.INITIATE_REJECTED, PaymentInitiateRejectedException::new)
                .detail("%s rejected the payment for order %s.", provider, orderRef)
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
