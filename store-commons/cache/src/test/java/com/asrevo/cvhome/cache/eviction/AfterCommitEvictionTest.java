package com.asrevo.cvhome.cache.eviction;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.asrevo.cvhome.cache.CacheKey;
import com.asrevo.cvhome.cache.CacheRegions;
import com.asrevo.cvhome.cache.CacheRegistry;
import com.asrevo.cvhome.cache.RegionCache;
import com.asrevo.cvhome.cache.Stores;
import com.asrevo.cvhome.cache.TestRegions;
import com.asrevo.cvhome.cache.config.CacheProperties;
import com.asrevo.cvhome.cache.provider.caffeine.CaffeineCacheProvider;

import static org.assertj.core.api.Assertions.assertThat;

class AfterCommitEvictionTest {

    private static final CacheKey A = CacheKey.of(Stores.A, Stores.EN);

    private static final String VALUE = "v";

    private final CacheRegistry registry = new CacheRegistry(CacheRegions.of(TestRegions.values()),
            List.of(new CaffeineCacheProvider()), CacheProperties.defaults());

    private final RegionCache<String> product = registry.region(TestRegions.PRODUCT, String.class);

    private final AfterCommitEviction eviction = new AfterCommitEviction(registry);

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void outsideATransactionTheStoreIsDroppedAtOnceInsideOneAfterTheCommit() {
        product.put(A, VALUE);
        eviction.evictStoreAfterCommit(Stores.A, List.of(TestRegions.PRODUCT));
        assertThat(product.getIfPresent(A)).isEmpty();

        product.put(A, VALUE);
        TransactionSynchronizationManager.initSynchronization();
        eviction.evictStoreAfterCommit(Stores.A, List.of(TestRegions.PRODUCT));
        assertThat(product.getIfPresent(A)).as("not before the commit").isPresent();
        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }
        assertThat(product.getIfPresent(A)).isEmpty();
    }
}
