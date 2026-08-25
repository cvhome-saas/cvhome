package com.asrevo.cvhome.tenancy.manager.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.podregistry.commons.dto.RecordPlacementRequest;
import com.asrevo.cvhome.podregistry.services.placement.ExternalPodPlacementService;
import com.asrevo.cvhome.tenancy.manager.repository.ManagerStoreRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Tells the pod registry about every store tenancy has placed, so its capacity counter matches reality.
 *
 * <p>
 * {@code pod_registry.pod.capacity_stores} is a <em>mirror</em> of a fact this service owns
 * ({@code manager_store.pod_id}), and its only writer is {@code PodCapacityEventImpl}, the outbox handler on
 * {@code StoreCreatedEvent}. So the count is right for every store created through the application and wrong for
 * every store that arrived another way — seed data, a direct insert, or anything created before that pipeline
 * existed. On a stack seeded with two stores the registry counted zero, and the pod screen said so.
 * </p>
 *
 * <p>
 * <strong>Why this lives in tenancy and not in pod-registry.</strong> The registry has no dependency on tenancy in
 * either direction, and it should not gain one: {@code PodStorePlacementEntity}'s own comment explains that the
 * duplicated fact exists precisely so the registry never reads tenancy's schema. Tenancy already depends on
 * {@code pod-registry-external-api} and already owns the client, so the replay costs no new coupling.
 * </p>
 *
 * <p>
 * <strong>Why it is scheduled rather than run once at startup.</strong> It was an {@code ApplicationReadyEvent}
 * hook first, and the very next boot proved that wrong: tenancy and the registry start in parallel, tenancy was
 * ready first, and the single attempt hit a registry that was not listening yet. "Ready" means <em>this</em>
 * service is ready, not the one it wants to talk to. Repeating on a timer also covers the case the console's
 * disagreement notice is really about — drift appearing <em>after</em> boot — so one mechanism answers both.
 * </p>
 *
 * <p>
 * Idempotency is the registry's, and is what makes repetition free: the claim is an insert on a primary key and
 * the count is recomputed rather than incremented, so a pass with nothing new writes nothing. One bulk call per
 * pass rather than one per store, for the same reason.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StorePlacementReconciler {

    private final ManagerStoreRepository storeRepository;

    private final ExternalPodPlacementService placementService;

    /**
     * Deliberately unhurried. Nothing here is on a request path, the registry ignores a pass with nothing new, and
     * the only cost of being a few minutes stale is a capacity figure — so the interval is set by how soon an
     * operator should stop seeing the disagreement notice, not by anything urgent.
     *
     * <p>
     * {@code fixedDelay} rather than {@code fixedRate}: the gap should be measured from the end of the last pass,
     * so a registry that is slow to answer does not queue passes behind each other.
     * </p>
     */
    @Scheduled(initialDelayString = "${com.asrevo.cvhome.tenancy.placement.reconcile-delay:PT30S}",
            fixedDelayString = "${com.asrevo.cvhome.tenancy.placement.reconcile-rate:PT15M}")
    public void reconcile() {
        List<RecordPlacementRequest> placed = storeRepository.findPlaced().stream()
                .map(store -> new RecordPlacementRequest(store.getId(), store.getPodId()))
                .toList();
        if (placed.isEmpty()) {
            return;
        }
        try {
            placementService.recordPlacements(placed);
            log.debug("Offered {} placement(s) to the pod registry for reconciliation", placed.size());
        } catch (Exception e) {
            /*
             * Deliberately broad, and deliberately swallowed. The registry being unreachable — or refusing, or
             * answering something unparseable — leaves its counter stale, which costs a wrong number on one screen
             * and a placement decision made against it. Neither is worth a stack trace every pass, and the next
             * pass tries again, so this is a warning with the reason and nothing more.
             */
            log.warn("Could not reconcile {} placement(s) with the pod registry: {}. Retrying on the next pass",
                    placed.size(), e.getMessage());
        }
    }

}
