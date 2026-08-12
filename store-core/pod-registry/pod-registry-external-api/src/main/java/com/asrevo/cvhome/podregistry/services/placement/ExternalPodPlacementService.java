package com.asrevo.cvhome.podregistry.services.placement;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.asrevo.cvhome.podregistry.api.errors.PodPlacementRefusedException;
import com.asrevo.cvhome.podregistry.api.errors.PodRegistryUnavailableException;
import com.asrevo.cvhome.podregistry.commons.dto.PlacementDecision;
import com.asrevo.cvhome.podregistry.commons.dto.PlacementRequest;

/**
 * What a caller of the placement API depends on. Blocking, for servlet callers such as tenancy — the reactive
 * pod-list contract the gateway uses is a separate interface, so a {@code Mono} never appears on this proxy.
 *
 * <p>
 * Nothing implements this: {@code RestClientBuilder.buildClient(...)} generates the proxy from it, and because the
 * error handler narrows a carrier only into types the invoked method declares, naming the caller-side exceptions
 * here is what makes them arrive as themselves rather than wrapped.
 * </p>
 *
 * <p>
 * The path below is not checked against the controller's mapping by any compiler. Keep them in step by eye.
 * </p>
 */
@HttpExchange("/api/v1/pod/private")
public interface ExternalPodPlacementService {

    /**
     * Where a new store for this organization should be created.
     *
     * @throws PodPlacementRefusedException    the registry decided nothing can take it — including when the
     *                                         organization's own private pods are all ineligible. Definitive: the
     *                                         caller stops rather than retries
     * @throws PodRegistryUnavailableException the registry could not be reached, so nothing was decided
     */
    @PostExchange("/placement")
    PlacementDecision place(@RequestBody PlacementRequest request)
            throws PodPlacementRefusedException, PodRegistryUnavailableException;

}
