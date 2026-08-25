package com.asrevo.cvhome.podregistry.api.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.RemoteServiceException;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;
import com.asrevo.cvhome.podregistry.commons.errors.PodRegistryErrors;

/**
 * The registry has nowhere to put this store.
 *
 * <p>
 * A definitive answer: retrying unchanged will be refused identically. Most often it means the organization's own
 * private pods are all draining, unhealthy or full — and the registry deliberately does <em>not</em> substitute a
 * shared pod in that case, so the caller has to surface it rather than work around it.
 * </p>
 *
 * <p>
 * That is what separates it from {@link PodRegistryUnavailableException}, where nothing was decided at all.
 * </p>
 */
public class PodPlacementRefusedException extends PodRegistryApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected PodPlacementRefusedException(ErrorPayload payload, Throwable cause, String remoteService,
            String remoteCode, int remoteStatus) {
        super(payload, cause, remoteService, remoteCode, remoteStatus);
    }

    public static PodPlacementRefusedException from(RemoteErrorContext context) {
        return RemoteServiceException.of(PodRegistryErrors.NO_ELIGIBLE_POD, PodPlacementRefusedException::new)
                .detail(context.detail() == null ? "No pod can take a new store for this organization."
                        : context.detail())
                .params(context.params())
                .cause(context.cause())
                .remoteService(POD_REGISTRY_SERVICE)
                .remoteCode(context.code())
                .remoteStatus(context.status())
                .build();
    }

}
