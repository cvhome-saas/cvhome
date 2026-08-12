package com.asrevo.cvhome.podregistry.api.errors;

import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;
import com.asrevo.cvhome.podregistry.commons.errors.PodRegistryErrors;

/**
 * The pod-registry API's error contract: which wire codes become which exception on a caller's side.
 *
 * <p>
 * Passed explicitly where the client is built, in the caller's {@code ClientsConfig}, rather than discovered from
 * the classpath — a service that calls the registry says so, instead of inheriting the contract because a jar
 * happens to be present.
 * </p>
 *
 * <p>
 * Codes are named by their enum rather than as string literals, so renaming one in {@link PodRegistryErrors} cannot
 * silently orphan a mapping here.
 * </p>
 */
public final class PodRegistryApiErrors {

    public static final RemoteErrorCatalog CATALOG = RemoteErrorCatalog.builder()
            // The registry decided. Definitive: the caller stops rather than retries.
            .map(PodRegistryErrors.NO_ELIGIBLE_POD, PodPlacementRefusedException::from)
            // No counterpart inside the registry exists for a call that never arrived.
            .unreachable(PodRegistryUnavailableException::from)
            .build();

    private PodRegistryApiErrors() {
    }

}
