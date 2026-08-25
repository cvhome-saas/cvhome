package com.asrevo.cvhome.payment.api.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.CommonErrors;
import com.asrevo.cvhome.errors.ErrorCodeAware;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ExternalProviderException;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;

/**
 * The payment service could not be reached, or answered in a way that carried no decision — connection refused, DNS
 * failure, read timeout, or a failure this SDK has no name for.
 *
 * <p>
 * The critical difference from {@link PaymentGatewayRejectedException}: <em>nothing was decided</em>. The payment may
 * have been started, or not, and this side cannot tell. A caller must treat the order as indeterminate — hold it and
 * reconcile — rather than declaring it failed, which is the mistake a single generic remote exception invites.
 * </p>
 *
 * <p>
 * The one exception here with no server-side counterpart, because a service that never answered never threw anything.
 * </p>
 */
public class PaymentApiUnavailableException extends PaymentApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected PaymentApiUnavailableException(ErrorPayload payload, Throwable cause, String remoteService,
            String remoteCode, int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

    /**
     * Built by {@code PaymentApiErrors.CATALOG} for the two failures that decide nothing: a call that produced no response
     * at all, and a {@code PAYMENT.INITIATE.FAILED} answer, which is the payment service reporting that <em>its</em>
     * provider never decided either. Different distances away, same instruction to the caller — do not assume the
     * payment failed — so the same type.
     */
    public static PaymentApiUnavailableException from(RemoteErrorContext context) {
        return RemoteServiceException.of(CommonErrors.REMOTE_UNAVAILABLE, PaymentApiUnavailableException::new)
                .detail(context.detail() == null ? "The payment service could not be reached." : context.detail())
                .params(context.params())
                .cause(context.cause())
                .remoteService(PAYMENT_SERVICE)
                .remoteCode(context.code())
                .remoteStatus(context.status())
                .build();
    }

    /**
     * Built by the client wrapper for a failure this SDK does not name — an unmapped code, or a response that was not a
     * problem document. Undecided either way, which is the only thing a caller can act on.
     */
    public static PaymentApiUnavailableException wrapping(Throwable cause) {
        RemoteServiceException.Builder<PaymentApiUnavailableException> builder =
                RemoteServiceException.of(CommonErrors.REMOTE_UNAVAILABLE, PaymentApiUnavailableException::new)
                        .detail("The payment service did not complete the request.")
                        .cause(cause)
                        .remoteService(PAYMENT_SERVICE);

        if (cause instanceof ErrorCodeAware aware) {
            builder.params(aware.params()).remoteCode(aware.errorCode().code());
        }
        if (cause instanceof RemoteServiceException remote) {
            builder.remoteStatus(remote.remoteStatus());
        } else if (cause instanceof ExternalProviderException provider) {
            // The status payment would have answered with, never the provider's own — the provider's belongs to a
            // conversation this caller was not part of, and travels in the params instead.
            builder.remoteStatus(provider.category().httpStatus());
        }
        return builder.build();
    }

}
