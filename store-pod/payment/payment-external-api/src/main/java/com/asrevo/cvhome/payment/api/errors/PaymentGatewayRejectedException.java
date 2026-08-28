package com.asrevo.cvhome.payment.api.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;
import com.asrevo.cvhome.payment.errors.PaymentErrors;
import com.asrevo.cvhome.payment.errors.PaymentInitiateRejectedException;

/**
 * The payment service refused to start the payment.
 *
 * <p>
 * A definitive answer: the payment will not happen, and retrying the same request will not change that. A caller must
 * unwind whatever it staged for this order, which is exactly what distinguishes it from
 * {@link PaymentApiUnavailableException}, where the payment may yet succeed.
 * </p>
 *
 * <p>
 * The caller-side counterpart of {@link PaymentInitiateRejectedException}. Both describe the same event from different
 * positions — "Stripe refused payment-service" and "payment-service refused us" — and keeping them apart is what lets
 * {@link #remoteService()} stay truthful on each side. The server's exception is always the cause, so its provider code
 * and params reach this side's logs intact.
 * </p>
 */
public class PaymentGatewayRejectedException extends PaymentApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected PaymentGatewayRejectedException(ErrorPayload payload, Throwable cause, String remoteService,
            String remoteCode, int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

    /**
     * Rebuilds the rejection on this side from the problem body the payment service sent — the entry point
     * {@code PaymentApiErrors.CATALOG} registers for {@code PAYMENT.INITIATE.REJECTED}.
     *
     * <p>
     * The caller-side type is built directly, rather than rebuilding the server's {@link PaymentInitiateRejectedException}
     * and translating it. A caller never spoke to Stripe, so it should never hold an exception whose {@code provider()}
     * claims it did; what it spoke to was payment, and that is what {@link #remoteService()} says here. The provider's
     * identity and code still arrive intact — the payment service sent them as params, and they are copied through.
     * </p>
     */
    public static PaymentGatewayRejectedException from(RemoteErrorContext context) {
        return RemoteServiceException.of(PaymentErrors.INITIATE_REJECTED, PaymentGatewayRejectedException::new)
                .detail(context.detail())
                .params(context.params())
                .remoteService(PAYMENT_SERVICE)
                .remoteCode(context.code())
                .remoteStatus(context.status())
                .build();
    }

    /**
     * Restates a rejection this JVM already holds as a typed object — the path taken when the delegate is a local
     * implementation rather than the HTTP proxy, since then no problem body was ever serialised.
     *
     * @param cause the exception the payment service threw
     */
    public static PaymentGatewayRejectedException wrapping(PaymentInitiateRejectedException cause) {
        return RemoteServiceException.of(PaymentErrors.INITIATE_REJECTED, PaymentGatewayRejectedException::new)
                .detail(cause.payload().detail())
                .params(cause.params())
                .cause(cause)
                // The service we called, not the gateway it called in turn: from here, payment is what refused.
                .remoteService(PAYMENT_SERVICE)
                .remoteCode(cause.errorCode().code())
                // The status payment-service would have answered with, not Stripe's. Stripe's 402 answered a question
                // this caller never asked, and reflecting it here would be the very leak ExternalProviderException
                // exists to prevent.
                .remoteStatus(PaymentErrors.INITIATE_REJECTED.category().httpStatus())
                .build();
    }

}
