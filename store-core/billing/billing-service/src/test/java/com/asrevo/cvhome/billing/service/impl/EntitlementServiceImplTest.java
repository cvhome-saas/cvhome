package com.asrevo.cvhome.billing.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.commons.dto.EntitlementSnapshot;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionNotFoundException;
import com.asrevo.cvhome.billing.domain.StoreSubscriptionEntity;
import com.asrevo.cvhome.billing.mappers.SubscriptionMappers;
import com.asrevo.cvhome.billing.repository.StoreSubscriptionRepository;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * What a store may do, answered often enough to need a cache in front of it.
 *
 * <p>
 * The cache is what these tests are mostly about: it is asked on every pod write path and by the gateway on a timer,
 * and its thirty-second window is chosen to bound how long a suspended store keeps working — not to save the
 * database. A batch read deliberately does not populate it, because batches are asked for by something rendering a
 * list and caching whole batches would evict the single-store entries the cache exists for.
 * </p>
 */
class EntitlementServiceImplTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final StoreMerchantId OTHER = new StoreMerchantId("65f023632bc46470c104b75f");

    private static final ManagerOrgId ORG = new ManagerOrgId("32a034a43cd77581d105c87a");

    private StoreSubscriptionRepository subscriptions;

    private EntitlementServiceImpl service;

    @BeforeEach
    void setUp() {
        subscriptions = mock(StoreSubscriptionRepository.class);
        SubscriptionMappers mappers = mock(SubscriptionMappers.class);
        when(mappers.toSnapshot(any(StoreSubscriptionEntity.class))).thenAnswer(it -> {
            StoreSubscriptionEntity entity = it.getArgument(0, StoreSubscriptionEntity.class);
            return new EntitlementSnapshot(entity.getId(), entity.getStatus(), entity.operable(), null, null,
                    Map.of());
        });
        service = new EntitlementServiceImpl(subscriptions, mappers);
    }

    @Test
    @DisplayName("a store's snapshot is read once and served from memory afterwards")
    void theSnapshotIsCached() throws Exception {
        when(subscriptions.findById(STORE))
                .thenReturn(Optional.of(StoreSubscriptionEntity.pending(STORE, ORG)));

        EntitlementSnapshot first = service.snapshot(STORE);
        EntitlementSnapshot second = service.snapshot(STORE);

        assertThat(second).isSameAs(first);
        verify(subscriptions, times(1)).findById(STORE);
    }

    @Test
    @DisplayName("the cache is per store, so one store's answer is never served for another")
    void theCacheIsKeyedByStore() throws Exception {
        when(subscriptions.findById(STORE))
                .thenReturn(Optional.of(StoreSubscriptionEntity.pending(STORE, ORG)));
        when(subscriptions.findById(OTHER))
                .thenReturn(Optional.of(StoreSubscriptionEntity.pending(OTHER, ORG)));

        assertThat(service.snapshot(STORE).store()).isEqualTo(STORE);
        assertThat(service.snapshot(OTHER).store()).isEqualTo(OTHER);
    }

    @Test
    @DisplayName("a store billing has never seen is reported, and the miss is not cached as an answer")
    void aMissingStoreIsReported() {
        when(subscriptions.findById(STORE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.snapshot(STORE)).isInstanceOf(SubscriptionNotFoundException.class);
        assertThatThrownBy(() -> service.snapshot(STORE)).isInstanceOf(SubscriptionNotFoundException.class);
        // Asked again rather than remembering the absence: a store provisioned a second later must not be refused
        // for the rest of the cache window.
        verify(subscriptions, times(2)).findById(STORE);
    }

    @Test
    @DisplayName("a batch read goes to the database and does not disturb the cache")
    void batchesAreNotCached() {
        StoreSubscriptionEntity entity = StoreSubscriptionEntity.pending(STORE, ORG);
        when(subscriptions.findAllByStoreIds(anyList())).thenReturn(List.of(entity));

        List<EntitlementSnapshot> snapshots = service.snapshots(List.of(STORE));

        assertThat(snapshots).hasSize(1);
        verify(subscriptions).findAllByStoreIds(List.of(STORE.getId().toString()));
    }

    @Test
    @DisplayName("an empty or null batch asks the database nothing")
    void anEmptyBatchIsShortCircuited() {
        assertThat(service.snapshots(List.of())).isEmpty();
        assertThat(service.snapshots(null)).isEmpty();

        verify(subscriptions, never()).findAllByStoreIds(anyList());
    }

    @Test
    @DisplayName("the blocked list is exactly the states that are not operable")
    void blockedStoresMirrorsOperable() {
        when(subscriptions.findAllByStatusIn(anyList()))
                .thenReturn(List.of(StoreSubscriptionEntity.pending(STORE, ORG)));

        assertThat(service.blockedStores()).containsExactly(STORE);

        // Derived from SubscriptionStatus.operable() rather than listed twice, so the query and the in-memory
        // answer cannot disagree — a store the gateway lets through but the pods refuse is the worst of both.
        List<SubscriptionStatus> blocked = List.of(SubscriptionStatus.PENDING, SubscriptionStatus.SUSPENDED,
                SubscriptionStatus.CANCELED);
        verify(subscriptions).findAllByStatusIn(blocked);
        assertThat(blocked).allSatisfy(status -> assertThat(status.operable()).isFalse());
        assertThat(java.util.Arrays.stream(SubscriptionStatus.values())
                .filter(it -> !blocked.contains(it))
                .toList())
                .allSatisfy(status -> assertThat(status.operable()).isTrue());
    }

}
