package com.asrevo.cvhome.podregistry;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
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
class PodCapacityServiceTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final StoreMerchantId OTHER_STORE = new StoreMerchantId("65f023632bc46470c104b75f");

    private static final StoreMerchantId THIRD_STORE = new StoreMerchantId("65f023632bc46470c104b77f");

    private static final PodId POD = new PodId("507f1f77bcf86cd799439011");

    private static final PodId OTHER_POD = new PodId("607f1f77bcf86cd799439022");

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


    /*
     * The bulk path, which the startup reconciliation uses. Its reason to exist is the recount: doing this as a
     * loop over `recordPlacement` would be correct and would recount a pod once per store sitting on it.
     */
    @Test
    @DisplayName("a bulk claim recounts each pod once, however many stores landed on it")
    void bulkRecountsEachPodOnce() {
        when(repository.claim(anyString(), anyString())).thenReturn(1);

        int claimed = service.recordPlacements(List.of(new RecordPlacementRequest(STORE, POD),
                new RecordPlacementRequest(OTHER_STORE, POD),
                new RecordPlacementRequest(THIRD_STORE, OTHER_POD)));

        assertThat(claimed).isEqualTo(3);
        verify(repository, times(1)).recountCapacity(POD.getId().toString());
        verify(repository, times(1)).recountCapacity(OTHER_POD.getId().toString());
    }

    @Test
    @DisplayName("a batch that is entirely redelivery writes nothing — which is what makes it safe on every boot")
    void bulkRedeliveryIsANoOp() {
        when(repository.claim(anyString(), anyString())).thenReturn(0);

        int claimed = service.recordPlacements(List.of(new RecordPlacementRequest(STORE, POD),
                new RecordPlacementRequest(OTHER_STORE, POD)));

        assertThat(claimed).isZero();
        verify(repository, never()).recountCapacity(anyString());
    }

    @Test
    @DisplayName("a partly-known batch recounts only the pods that actually gained a store")
    void bulkRecountsOnlyWhatChanged() {
        when(repository.claim(STORE.getId().toString(), POD.getId().toString())).thenReturn(0);
        when(repository.claim(THIRD_STORE.getId().toString(), OTHER_POD.getId().toString())).thenReturn(1);

        int claimed = service.recordPlacements(List.of(new RecordPlacementRequest(STORE, POD),
                new RecordPlacementRequest(THIRD_STORE, OTHER_POD)));

        assertThat(claimed).isEqualTo(1);
        verify(repository, never()).recountCapacity(POD.getId().toString());
        verify(repository).recountCapacity(OTHER_POD.getId().toString());
    }

    @Test
    @DisplayName("an empty batch does not reach the database at all")
    void emptyBulkIsIgnored() {
        assertThat(service.recordPlacements(List.of())).isZero();

        verify(repository, never()).claim(anyString(), anyString());
        verify(repository, never()).recountCapacity(anyString());
    }

}
