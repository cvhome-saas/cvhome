package com.asrevo.cvhome.podregistry.services.placement;

import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.asrevo.cvhome.podregistry.api.errors.PodPlacementRefusedException;
import com.asrevo.cvhome.podregistry.api.errors.PodRegistryUnavailableException;
import com.asrevo.cvhome.podregistry.commons.dto.PlacementDecision;
import com.asrevo.cvhome.podregistry.commons.dto.PlacementRequest;
import com.asrevo.cvhome.podregistry.commons.dto.RecordPlacementRequest;

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

    /**
     * Tells the registry a store actually landed, so it can count it against the pod's capacity.
     *
     * <p>
     * Called after the store row is committed, from an outbox handler, so it inherits durable retries — which is
     * also why the registry makes it idempotent. It declares only the unavailable failure: there is no refusal to
     * express, because by this point the store exists whether the registry likes it or not.
     * </p>
     *
     * @throws PodRegistryUnavailableException the registry could not be reached, so the count is not yet updated;
     *                                         the caller should let the outbox retry rather than swallow it
     */
    @PostExchange("/placement-recorded")
    void recordPlacement(@RequestBody RecordPlacementRequest request) throws PodRegistryUnavailableException;

    /**
     * Tells the registry about many placements at once, so it can reconcile a counter it maintains as a mirror.
     *
     * <p>
     * Used by the startup reconciliation, not by the outbox: the outbox has one store in hand and durable retries,
     * while this replays everything tenancy has placed and would otherwise be one round trip per store on every
     * boot. The registry makes it idempotent, so replaying the whole set is safe.
     * </p>
     *
     * @throws PodRegistryUnavailableException the registry could not be reached, so nothing was reconciled. The
     *                                         caller should log and move on — the next boot tries again, and the
     *                                         counter being stale is not a reason to hold a service up
     */
    @PostExchange("/placements-recorded")
    void recordPlacements(@RequestBody List<RecordPlacementRequest> requests) throws PodRegistryUnavailableException;

}
