package com.asrevo.cvhome.tenancy.manager.processors.event;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.errors.UncheckedBaseException;
import com.asrevo.cvhome.podregistry.api.errors.PodRegistryUnavailableException;
import com.asrevo.cvhome.podregistry.commons.dto.RecordPlacementRequest;
import com.asrevo.cvhome.podregistry.services.placement.ExternalPodPlacementService;
import com.asrevo.cvhome.tenancy.events.store.StoreCreatedEvent;

import io.namastack.outbox.annotation.OutboxHandler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Tells the pod registry that a store landed, so it can count it against the pod's capacity.
 *
 * <p>
 * A third handler on {@code StoreCreatedEvent}, alongside pod provisioning and billing. The outbox writes one
 * record per handler, so all three retry independently: a registry that is down cannot stop the store being built
 * or billed, and vice versa.
 * </p>
 *
 * <p>
 * Deliberately not folded into the placement call that chose the pod. That call happens while {@code createStore}
 * is still deciding, and the creation can still fail after it — counting there would leak capacity on every
 * abandoned attempt. Counting from the committed event means the number reflects stores that exist.
 * </p>
 */
@Service
@AllArgsConstructor
@Slf4j
public class PodCapacityEventImpl implements EventImpl<StoreCreatedEvent> {

    private final ExternalPodPlacementService placementService;

    /**
     * Lets an unreachable registry propagate so the outbox retries.
     *
     * <p>
     * There is no refusal to catch here, unlike the billing handler: by this point the store exists, and the
     * registry's only job is to notice. Its endpoint is idempotent on the store id, so a retry after a partial
     * failure costs nothing.
     * </p>
     */
    @Override
    @OutboxHandler
    public void process(StoreCreatedEvent event) {
        try {
            placementService.recordPlacement(new RecordPlacementRequest(event.store(), event.podId()));
            log.info("Recorded store {} on pod {} with the registry", event.store(), event.podId());
        } catch (PodRegistryUnavailableException e) {
            // Rethrown unchecked because the handler signature cannot declare it. The outbox sees a failure and
            // retries, which is right: the registry was unreachable, and a miscounted pod misplaces later stores.
            throw new UncheckedBaseException(e);
        }
    }

}
