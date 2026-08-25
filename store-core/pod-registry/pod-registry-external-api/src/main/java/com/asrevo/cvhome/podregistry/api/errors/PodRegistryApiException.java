package com.asrevo.cvhome.podregistry.api.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.RemoteServiceException;

/**
 * Base of the failures a caller of the pod-registry API can receive.
 *
 * <p>
 * Caller-side counterparts of the registry's own exceptions. The registry's {@code NoEligiblePodException} means
 * <em>the registry</em> found nothing suitable; {@link PodPlacementRefusedException} is how that reaches us. Keeping
 * them separate is what lets {@code remoteService} name the service that actually decided.
 * </p>
 *
 * <p>
 * Catch this for "the placement call failed, however"; catch a subclass to act on a particular answer.
 * </p>
 */
public abstract class PodRegistryApiException extends RemoteServiceException {

    /** The service these failures are reported against, from this side of the call. */
    protected static final String POD_REGISTRY_SERVICE = "pod-registry";

    @Serial
    private static final long serialVersionUID = 1L;

    protected PodRegistryApiException(ErrorPayload payload, Throwable cause, String remoteService, String remoteCode,
            int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

}
