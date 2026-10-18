package com.asrevo.cvhome.cache.eviction;

import java.util.Collection;
import java.util.List;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.asrevo.cvhome.cache.CacheRegion;
import com.asrevo.cvhome.cache.CacheRegistry;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * For the writes Hibernate reports no entity for: a bulk JPQL update, a native refresh of a derived table. The
 * code that runs one calls this, and the store's entries drop once the surrounding transaction commits, or at
 * once outside one.
 */
public final class AfterCommitEviction {

    private final CacheRegistry registry;

    public AfterCommitEviction(CacheRegistry registry) {
        this.registry = registry;
    }

    public void evictStoreAfterCommit(StoreMerchantId store, Collection<? extends CacheRegion> regions) {
        List<CacheRegion> named = List.copyOf(regions);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    registry.evictStore(store, named);
                }
            });
        } else {
            registry.evictStore(store, named);
        }
    }
}
