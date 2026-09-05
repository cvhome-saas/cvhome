package com.asrevo.cvhome.checkout.api.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.CommonErrors;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;

/**
 * Checkout could not be reached or answered with no decision. A caller's outbox retries it; that is the whole point
 * of signalling through the outbox rather than in the webhook request.
 */
public class CheckoutApiUnavailableException extends CheckoutApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected CheckoutApiUnavailableException(ErrorPayload payload, Throwable cause, String remoteService,
                                              String remoteCode, int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

    public static CheckoutApiUnavailableException from(RemoteErrorContext context) {
        return RemoteServiceException.of(CommonErrors.REMOTE_UNAVAILABLE, CheckoutApiUnavailableException::new)
                .detail(context.detail() == null ? "The checkout service could not be reached." : context.detail())
                .params(context.params())
                .cause(context.cause())
                .remoteService(CHECKOUT_SERVICE)
                .remoteCode(context.code())
                .remoteStatus(context.status())
                .build();
    }

}
