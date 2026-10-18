package com.asrevo.cvhome.cache.provider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * A version per store for one region: dropping a store's entries is bumping its version.
 *
 * <p>
 * Every entry is stored under its key and the store's version at the time; a read looks the current version up
 * first, so entries written before a bump are never seen again and die of age or size. That makes a store's
 * eviction one increment whatever the provider (a remote one keeps the counter as a key of its own), where
 * scanning the keys cost the whole region on every merchant's save.
 * </p>
 */
public final class StoreVersions {

    private final Map<StoreMerchantId, AtomicLong> versions = new ConcurrentHashMap<>();

    public long current(StoreMerchantId store) {
        AtomicLong version = versions.get(store);
        return version == null ? 0 : version.get();
    }

    public long bump(StoreMerchantId store) {
        return versions.computeIfAbsent(store, ignored -> new AtomicLong()).incrementAndGet();
    }

    public void clear() {
        versions.clear();
    }
}
