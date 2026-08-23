package com.asrevo.cvhome.podregistry.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.podregistry.commons.dto.RecordPlacementRequest;
import com.asrevo.cvhome.podregistry.repository.PodStorePlacementRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Keeps {@code pod.capacity_stores} in step with the stores actually placed.
 *
 * <p>
 * Tenancy tells the registry a store landed, from an outbox handler — the same shape that provisions billing. That
 * makes delivery durable and retried, which in turn makes idempotency mandatory rather than nice to have: this is
 * called more than once for the same store as a matter of course, not as an edge case.
 * </p>
 *
 * <p>
 * Idempotency is a primary key, not an {@code if}. The placement row is claimed with
 * {@code insert … on conflict do nothing}; a redelivery affects zero rows and returns before touching the count.
 * The count is then <em>recomputed</em> from the placement table rather than incremented, so even a bug here
 * converges on the truth instead of accumulating error.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PodCapacityService {

    private final PodStorePlacementRepository placementRepository;

    /**
     * @return whether this call recorded a new placement; false means it was a redelivery and nothing changed
     */
    @Transactional
    public boolean recordPlacement(RecordPlacementRequest request) {
        StoreMerchantId store = request.store();
        PodId pod = request.pod();
        int claimed = placementRepository.claim(store.getId().toString(), pod.getId().toString());
        if (claimed == 0) {
            log.debug("Store {} was already recorded on a pod; capacity left alone", store);
            return false;
        }
        placementRepository.recountCapacity(pod.getId().toString());
        log.info("Store {} recorded on pod {}; capacity recounted", store, pod);
        return true;
    }

    /**
     * Records many placements at once, recounting each affected pod exactly once.
     *
     * <p>
     * For the reconciliation tenancy runs at startup, which replays every store it has placed. Calling
     * {@link #recordPlacement} in a loop would be correct but would recount a pod once per store on it; here the
     * claims happen first and the recount follows, per pod, after they have all landed.
     * </p>
     *
     * <p>
     * Idempotent for the same reason the single-store path is: the claim is an insert on a primary key and the
     * count is recomputed rather than incremented. A batch that is entirely redelivery claims nothing and recounts
     * nothing, which is what makes it safe to run on every boot.
     * </p>
     *
     * @return how many placements were new
     */
    @Transactional
    public int recordPlacements(List<RecordPlacementRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return 0;
        }
        Set<String> touched = new HashSet<>();
        int claimed = 0;
        for (RecordPlacementRequest request : requests) {
            String pod = request.pod().getId().toString();
            if (placementRepository.claim(request.store().getId().toString(), pod) > 0) {
                touched.add(pod);
                claimed++;
            }
        }
        touched.forEach(placementRepository::recountCapacity);
        if (claimed > 0) {
            log.info("Recorded {} new placement(s) of {} offered; recounted {} pod(s)",
                    claimed, requests.size(), touched.size());
        } else {
            log.debug("All {} offered placement(s) were already known; nothing recounted", requests.size());
        }
        return claimed;
    }

}
