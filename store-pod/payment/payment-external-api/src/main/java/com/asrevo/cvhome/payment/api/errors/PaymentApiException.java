package com.asrevo.cvhome.payment.api.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.payment.services.payment.ExternalPaymentGatewayService;

/**
 * Base of the failures a caller of {@link ExternalPaymentGatewayService} can receive.
 *
 * <p>
 * These are the client-SDK counterparts of the payment service's own exceptions, and the distinction is deliberate:
 * {@code PaymentInitiateRejectedException} in {@code payment-commons} means <em>Stripe</em> refused payment-service,
 * while {@link PaymentGatewayRejectedException} here means <em>payment-service</em> refused us. Sharing one class for
 * both would make {@code remoteService} read "stripe" on one hop and "payment" on the next, and no caller could tell
 * which system actually declined.
 * </p>
 *
 * <p>
 * Catch this type for "the payment API failed, however"; catch a subclass to act on a specific answer.
 * </p>
 */
public abstract class PaymentApiException extends RemoteServiceException {

    /**
     * The service these failures are reported against, from this side of the call.
     */
    protected static final String PAYMENT_SERVICE = "payment";

    @Serial
    private static final long serialVersionUID = 1L;

    protected PaymentApiException(ErrorPayload payload, Throwable cause, String remoteService, String remoteCode,
            int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

}
