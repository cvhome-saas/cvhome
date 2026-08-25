package com.asrevo.cvhome.tenancy.manager.service;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.podregistry.api.errors.PodRegistryUnavailableException;
import com.asrevo.cvhome.podregistry.commons.dto.RecordPlacementRequest;
import com.asrevo.cvhome.podregistry.services.placement.ExternalPodPlacementService;
import com.asrevo.cvhome.tenancy.manager.entity.ManagerStoreEntity;
import com.asrevo.cvhome.tenancy.manager.repository.ManagerStoreRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Reconciling the registry's capacity mirror with the stores tenancy has actually placed.
 *
 * <p>
 * The bug this exists for: {@code pod.capacity_stores} is written only by the outbox handler on
 * {@code StoreCreatedEvent}, so a stack seeded with two stores had a pod counting zero — and the console said so.
 * </p>
 */
class StorePlacementReconcilerTest {

    private static final PodId POD = new PodId("507f1f77bcf86cd799439011");

    private static final PodId OTHER_POD = new PodId("607f1f77bcf86cd799439022");

    private static final ManagerOrgId ORG = new ManagerOrgId("21f023932bc66470c104b76f");

    private static final StoreMerchantId STORE_ONE = new StoreMerchantId("ORG1-STORE1");

    private static final StoreMerchantId STORE_TWO = new StoreMerchantId("ORG1-STORE2");

    private ManagerStoreRepository repository;

    private ExternalPodPlacementService placementService;

    private StorePlacementReconciler reconciler;

    private static ManagerStoreEntity store(StoreMerchantId id, PodId pod) {
        ManagerStoreEntity entity = new ManagerStoreEntity();
        entity.setId(id);
        entity.setOrgId(ORG);
        entity.setPodId(pod);
        entity.setName(id.storeMerchantId());
        return entity;
    }

    @BeforeEach
    void setUp() {
        repository = mock(ManagerStoreRepository.class);
        placementService = mock(ExternalPodPlacementService.class);
        reconciler = new StorePlacementReconciler(repository, placementService);
    }

    @Test
    @DisplayName("offers every placed store to the registry, in one call")
    void replaysEveryPlacedStore() throws Exception {
        when(repository.findPlaced()).thenReturn(List.of(store(STORE_ONE, POD), store(STORE_TWO, OTHER_POD)));

        reconciler.reconcile();

        ArgumentCaptor<List<RecordPlacementRequest>> sent = ArgumentCaptor.captor();
        // One call, not one per store: this runs on every boot, and a round trip per store does not scale.
        verify(placementService).recordPlacements(sent.capture());
        assertThat(sent.getValue())
                .containsExactly(new RecordPlacementRequest(STORE_ONE, POD),
                        new RecordPlacementRequest(STORE_TWO, OTHER_POD));
    }

    @Test
    @DisplayName("says nothing to the registry when there is nothing placed")
    void quietWhenThereIsNothingToSay() throws Exception {
        when(repository.findPlaced()).thenReturn(List.of());

        reconciler.reconcile();

        verify(placementService, never()).recordPlacements(any());
    }

    /*
     * Throwing out of a scheduled method kills the schedule for good in some configurations, and a stale capacity
     * counter costs a wrong number on one screen. This is exactly what happened on the first real boot: tenancy
     * came up before pod-registry was listening, and the pass has to survive that and try again.
     */
    @Test
    @DisplayName("an unreachable registry costs this pass and nothing else")
    void survivesAnUnreachableRegistry() throws Exception {
        when(repository.findPlaced()).thenReturn(List.of(store(STORE_ONE, POD)));
        doThrow(PodRegistryUnavailableException.class).when(placementService).recordPlacements(any());

        assertThatCode(() -> reconciler.reconcile()).doesNotThrowAnyException();
    }

}
