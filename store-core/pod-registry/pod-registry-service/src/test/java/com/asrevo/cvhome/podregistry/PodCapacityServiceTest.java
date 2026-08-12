package com.asrevo.cvhome.podregistry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.podregistry.commons.dto.RecordPlacementRequest;
import com.asrevo.cvhome.podregistry.repository.PodStorePlacementRepository;
import com.asrevo.cvhome.podregistry.service.PodCapacityService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Capacity counting, and specifically its idempotency.
 *
 * <p>
 * This is driven from tenancy's outbox, which retries — so being called twice for the same store is the normal
 * case, not an edge one. {@code capacity_stores = capacity_stores + 1} would drift on every redelivery; these pin
 * that it does not.
 * </p>
 */
@Tag("unit-test")
class PodCapacityServiceTest {

    private static final ManagerStoreId STORE = new ManagerStoreId("65f023632bc46470c104b76f");

    private static final PodId POD = new PodId("507f1f77bcf86cd799439011");

    private PodStorePlacementRepository repository;

    private PodCapacityService service;

    @BeforeEach
    void setUp() {
        repository = mock(PodStorePlacementRepository.class);
        service = new PodCapacityService(repository);
    }

    @Test
    @DisplayName("a first placement is claimed and the pod is recounted")
    void firstPlacementCounts() {
        when(repository.claim(anyString(), anyString())).thenReturn(1);

        assertThat(service.recordPlacement(new RecordPlacementRequest(STORE, POD))).isTrue();

        verify(repository).recountCapacity(POD.getId().toString());
    }

    @Test
    @DisplayName("a redelivery changes nothing — the claim affects no rows and the count is not touched")
    void redeliveryIsANoOp() {
        when(repository.claim(anyString(), anyString())).thenReturn(0);

        assertThat(service.recordPlacement(new RecordPlacementRequest(STORE, POD))).isFalse();

        // The important half: no recount, so a retried outbox record cannot inflate the pod's store count.
        verify(repository, never()).recountCapacity(anyString());
    }

    @Test
    @DisplayName("replaying the same event repeatedly still recounts exactly once")
    void repeatedReplayCountsOnce() {
        when(repository.claim(anyString(), anyString())).thenReturn(1).thenReturn(0).thenReturn(0);
        RecordPlacementRequest request = new RecordPlacementRequest(STORE, POD);

        service.recordPlacement(request);
        service.recordPlacement(request);
        service.recordPlacement(request);

        verify(repository, times(1)).recountCapacity(anyString());
    }

}
