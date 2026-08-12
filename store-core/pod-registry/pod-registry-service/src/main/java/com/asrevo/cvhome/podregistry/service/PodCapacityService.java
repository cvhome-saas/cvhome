package com.asrevo.cvhome.podregistry.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.PodId;
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
        ManagerStoreId store = request.store();
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

}
