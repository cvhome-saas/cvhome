package com.asrevo.cvhome.podregistry.services.pod;

import java.util.List;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.podregistry.api.errors.PodRegistryUnavailableException;

/**
 * The pod list for blocking callers — the servlet counterpart of {@code ReactiveExternalPodService}.
 *
 * <p>
 * Deliberately only a list, with no by-id method. The registry's {@code GET /api/v1/pod/{id}} answers the full
 * {@code PodView} and is super-admin only, because lifecycle, capacity and health are operator data; a service
 * principal asking "where does this store live" has no business reading them. The list is the routing-level
 * {@link Pod}, it is short, and one fetch answers every lookup — which is what {@link CachingPodDirectory} is
 * built on.
 * </p>
 */
@HttpExchange("/api/v1/pod")
public interface ExternalPodService {

    /**
     * @throws PodRegistryUnavailableException the registry could not be reached. Callers of this contract are
     *                                         expected to degrade rather than fail — see {@link CachingPodDirectory}
     */
    @GetExchange("list")
    List<Pod> listPods() throws PodRegistryUnavailableException;

}
