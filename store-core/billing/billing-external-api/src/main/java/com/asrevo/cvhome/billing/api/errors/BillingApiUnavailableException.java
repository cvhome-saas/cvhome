package com.asrevo.cvhome.billing.api.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.CommonErrors;
import com.asrevo.cvhome.errors.ErrorCodeAware;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ExternalProviderException;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;

/**
 * Billing could not be reached, or answered in a way that carried no decision.
 *
 * <p>
 * What the caller does with this is the interesting part, and it differs by caller. Store creation treats it as a
 * refusal and <em>fails closed</em>: creating a store nobody is billed for is worse than a retryable error. The
 * enforcement layers do the opposite and fail open, because an outage in billing must not take working stores
 * offline. Both are deliberate; neither is a default.
 * </p>
 *
 * <p>
 * The one exception here with no counterpart inside billing — a service that never answered never threw anything.
 * </p>
 */
public class BillingApiUnavailableException extends BillingApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected BillingApiUnavailableException(ErrorPayload payload, Throwable cause, String remoteService,
            String remoteCode, int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

    /**
     * Built by {@code BillingApiErrors.CATALOG} for a call that produced no usable answer.
     */
    public static BillingApiUnavailableException from(RemoteErrorContext context) {
        return RemoteServiceException.of(CommonErrors.REMOTE_UNAVAILABLE, BillingApiUnavailableException::new)
                .detail(context.detail() == null ? "The billing service could not be reached." : context.detail())
                .params(context.params())
                .cause(context.cause())
                .remoteService(BILLING_SERVICE)
                .remoteCode(context.code())
                .remoteStatus(context.status())
                .build();
    }

    /**
     * Built for a failure this SDK does not name — an unmapped code, or a response that was not a problem document.
     */
    public static BillingApiUnavailableException wrapping(Throwable cause) {
        RemoteServiceException.Builder<BillingApiUnavailableException> builder =
                RemoteServiceException.of(CommonErrors.REMOTE_UNAVAILABLE, BillingApiUnavailableException::new)
                        .detail("The billing service did not complete the request.")
                        .cause(cause)
                        .remoteService(BILLING_SERVICE);

        if (cause instanceof ErrorCodeAware aware) {
            builder.params(aware.params()).remoteCode(aware.errorCode().code());
        }
        if (cause instanceof RemoteServiceException remote) {
            builder.remoteStatus(remote.remoteStatus());
        } else if (cause instanceof ExternalProviderException provider) {
            // Billing's own status, never Stripe's — the provider's belongs to a conversation this caller was not
            // part of, and travels in the params instead.
            builder.remoteStatus(provider.category().httpStatus());
        }
        return builder.build();
    }

}
