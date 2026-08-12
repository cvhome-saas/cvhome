package com.asrevo.cvhome.billing.service.impl;

import java.time.Duration;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.commons.dto.EntitlementSnapshot;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionNotFoundException;
import com.asrevo.cvhome.billing.domain.StoreSubscriptionEntity;
import com.asrevo.cvhome.billing.mappers.SubscriptionMappers;
import com.asrevo.cvhome.billing.repository.StoreSubscriptionRepository;
import com.asrevo.cvhome.billing.service.EntitlementService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.extern.slf4j.Slf4j;

/**
 * Answers what a store may do, cheaply enough to be asked constantly.
 *
 * <p>
 * A short cache sits in front of the query because the pods ask on write paths and the gateway asks on a timer, and
 * the answer changes only when a subscription transitions. Thirty seconds is chosen to bound how long a suspended
 * store keeps working, not to save the database — a plan lookup is three small reads.
 * </p>
 */
@Slf4j
@Service
public class EntitlementServiceImpl implements EntitlementService {

    /**
     * The states in which a store may not be worked in. Derived from {@link SubscriptionStatus#operable()} rather
     * than listed twice, so the query and the in-memory answer cannot disagree.
     */
    private static final List<SubscriptionStatus> BLOCKED = List.of(SubscriptionStatus.PENDING,
            SubscriptionStatus.SUSPENDED, SubscriptionStatus.CANCELED);

    private final StoreSubscriptionRepository subscriptionRepository;

    private final SubscriptionMappers mappers;

    private final Cache<StoreMerchantId, EntitlementSnapshot> cache;

    public EntitlementServiceImpl(StoreSubscriptionRepository subscriptionRepository, SubscriptionMappers mappers) {
        this.subscriptionRepository = subscriptionRepository;
        this.mappers = mappers;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(30L))
                .maximumSize(50_000L)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EntitlementSnapshot snapshot(StoreMerchantId store) throws SubscriptionNotFoundException {
        EntitlementSnapshot cached = cache.getIfPresent(store);
        if (cached != null) {
            return cached;
        }
        EntitlementSnapshot snapshot = mappers.toSnapshot(subscriptionRepository.findById(store)
                .orElseThrow(() -> SubscriptionNotFoundException.forStore(store)));
        cache.put(store, snapshot);
        return snapshot;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntitlementSnapshot> snapshots(List<StoreMerchantId> stores) {
        if (stores == null || stores.isEmpty()) {
            return List.of();
        }
        // Not cached: a batch is asked for by something rendering a list, which is far rarer than the single-store
        // reads the cache exists for, and caching whole batches would evict the entries that matter.
        List<String> ids = stores.stream().map(it -> it.getId().toString()).toList();
        return subscriptionRepository.findAllByStoreIds(ids).stream().map(mappers::toSnapshot).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoreMerchantId> blockedStores() {
        return subscriptionRepository.findAllByStatusIn(BLOCKED).stream()
                .map(StoreSubscriptionEntity::getId)
                .toList();
    }

}
