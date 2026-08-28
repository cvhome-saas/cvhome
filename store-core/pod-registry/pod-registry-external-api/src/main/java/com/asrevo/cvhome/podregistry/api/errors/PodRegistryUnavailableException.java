package com.asrevo.cvhome.podregistry.api.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.CommonErrors;
import com.asrevo.cvhome.errors.ErrorCodeAware;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;

/**
 * The registry could not be reached, or answered in a way that carried no decision.
 *
 * <p>
 * Store creation <em>fails closed</em> on this, matching how it already treats billing being unreachable. The
 * asymmetry with the gateway is deliberate and worth stating: the gateway fails <em>open</em> on the same service
 * being down, because stale routes keep storefronts alive, while a store placed on a pod nobody confirmed is
 * eligible may land on hardware that is draining or full — and that is not recoverable by retrying later, because
 * the store is already there.
 * </p>
 *
 * <p>
 * The one exception here with no counterpart inside the registry — a service that never answered never threw
 * anything.
 * </p>
 */
public class PodRegistryUnavailableException extends PodRegistryApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected PodRegistryUnavailableException(ErrorPayload payload, Throwable cause, String remoteService,
            String remoteCode, int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

    /** Built by {@code PodRegistryApiErrors.CATALOG} for a call that produced no usable answer. */
    public static PodRegistryUnavailableException from(RemoteErrorContext context) {
        return RemoteServiceException.of(CommonErrors.REMOTE_UNAVAILABLE, PodRegistryUnavailableException::new)
                .detail(context.detail() == null ? "The pod registry could not be reached." : context.detail())
                .params(context.params())
                .cause(context.cause())
                .remoteService(POD_REGISTRY_SERVICE)
                .remoteCode(context.code())
                .remoteStatus(context.status())
                .build();
    }

    /** Built for a failure this SDK does not name — an unmapped code, or a response that was not a problem document. */
    public static PodRegistryUnavailableException wrapping(Throwable cause) {
        RemoteServiceException.Builder<PodRegistryUnavailableException> builder =
                RemoteServiceException.of(CommonErrors.REMOTE_UNAVAILABLE, PodRegistryUnavailableException::new)
                        .detail("The pod registry did not complete the request.")
                        .cause(cause)
                        .remoteService(POD_REGISTRY_SERVICE);

        if (cause instanceof ErrorCodeAware aware) {
            builder.params(aware.params()).remoteCode(aware.errorCode().code());
        }
        if (cause instanceof RemoteServiceException remote) {
            builder.remoteStatus(remote.remoteStatus());
        }
        return builder.build();
    }

}
