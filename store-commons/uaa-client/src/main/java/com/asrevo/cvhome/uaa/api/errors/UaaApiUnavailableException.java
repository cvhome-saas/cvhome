package com.asrevo.cvhome.uaa.api.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.CommonErrors;
import com.asrevo.cvhome.errors.ErrorCodeAware;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;

/**
 * uaa could not be reached, or answered in a way that decided nothing.
 *
 * <p>
 * The one type here with no server-side counterpart, because a service that never answered never threw anything. It
 * covers three routes to the same instruction — a refused connection or read timeout, a failed
 * {@code client_credentials} token exchange, and an answer this SDK has no name for — and the instruction is: nothing
 * was decided, so do not record the outcome as either success or refusal.
 * </p>
 *
 * <p>
 * A token failure belongs here rather than under an authentication type on purpose. From the caller's point of view
 * the request it asked for never happened, and whether that was because uaa was down or because our own client
 * credentials are wrong is an operational question, not something the caller can act on differently.
 * </p>
 */
public class UaaApiUnavailableException extends UaaApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected UaaApiUnavailableException(ErrorPayload payload, Throwable cause, String remoteService,
            String remoteCode, int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

    /**
     * Built by {@code UaaApiErrors.CATALOG} for a call that produced no response at all.
     */
    public static UaaApiUnavailableException from(RemoteErrorContext context) {
        return RemoteServiceException.of(CommonErrors.REMOTE_UNAVAILABLE, UaaApiUnavailableException::new)
                .detail(context.detail() == null ? "The uaa service could not be reached." : context.detail())
                .params(context.params())
                .cause(context.cause())
                .remoteService(UAA_SERVICE)
                .remoteCode(context.code())
                .remoteStatus(context.status())
                .build();
    }

    /**
     * Built by the SDK for a failure it does not name — an unmapped code, or a response that was not a problem
     * document. Undecided either way, which is the only thing a caller can act on.
     */
    public static UaaApiUnavailableException wrapping(Throwable cause) {
        RemoteServiceException.Builder<UaaApiUnavailableException> builder =
                RemoteServiceException.of(CommonErrors.REMOTE_UNAVAILABLE, UaaApiUnavailableException::new)
                        .detail("The uaa service did not complete the request.")
                        .cause(cause)
                        .remoteService(UAA_SERVICE);

        if (cause instanceof ErrorCodeAware aware) {
            builder.params(aware.params()).remoteCode(aware.errorCode().code());
        }
        if (cause instanceof RemoteServiceException remote) {
            // Keep what uaa actually answered: an unmapped 409 is still a 409, and the advice re-emits on that.
            builder.remoteCode(remote.remoteCode()).remoteStatus(remote.remoteStatus());
        }
        return builder.build();
    }

    /**
     * Built when the {@code client_credentials} exchange with uaa fails, so the request the caller wanted was never
     * even attempted.
     */
    public static UaaApiUnavailableException tokenRequestFailed(String detail, Throwable cause) {
        return RemoteServiceException.of(CommonErrors.REMOTE_UNAVAILABLE, UaaApiUnavailableException::new)
                .detail(detail)
                .param("service", UAA_SERVICE)
                .param("phase", "token")
                .cause(cause)
                .remoteService(UAA_SERVICE)
                .build();
    }

}
