package com.asrevo.cvhome.cache.provider.caffeine;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.asrevo.cvhome.cache.CacheKey;
import com.asrevo.cvhome.cache.RegionCache;
import com.asrevo.cvhome.cache.RegionStats;
import com.asrevo.cvhome.cache.provider.StoreVersions;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;

/**
 * A region on one Caffeine cache, each entry stored under its key and its store's version
 * ({@link StoreVersions}); a bulk read asks the loader once for the keys that miss.
 *
 * @param <V> the region's value type
 */
final class CaffeineRegionCache<V> implements RegionCache<V> {

    private final String name;

    private final Cache<VersionedKey, V> cache;

    private final StoreVersions versions = new StoreVersions();

    CaffeineRegionCache(String name, Cache<VersionedKey, V> cache) {
        this.name = name;
        this.cache = cache;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Optional<V> getIfPresent(CacheKey key) {
        return Optional.ofNullable(cache.getIfPresent(versioned(key)));
    }

    @Override
    public V get(CacheKey key, Function<CacheKey, V> loader) {
        return cache.get(versioned(key), stamped -> loader.apply(stamped.key()));
    }

    @Override
    public Map<CacheKey, V> getAll(Collection<CacheKey> keys, Function<Set<CacheKey>, Map<CacheKey, V>> loader) {
        Set<CacheKey> distinct = new LinkedHashSet<>(keys);
        if (distinct.isEmpty()) {
            return Map.of();
        }
        Map<VersionedKey, CacheKey> asked = new LinkedHashMap<>();
        for (CacheKey key : distinct) {
            asked.put(versioned(key), key);
        }
        Map<VersionedKey, V> found = cache.getAll(asked.keySet(), missing -> {
            Set<CacheKey> plain = new LinkedHashSet<>();
            for (VersionedKey stamped : missing) {
                plain.add(stamped.key());
            }
            Map<CacheKey, V> loaded = loader.apply(plain);
            Map<VersionedKey, V> stamped = new LinkedHashMap<>();
            for (Map.Entry<CacheKey, V> entry : loaded.entrySet()) {
                if (entry.getValue() != null) {
                    stamped.put(versioned(entry.getKey()), entry.getValue());
                }
            }
            return stamped;
        });
        Map<CacheKey, V> result = new LinkedHashMap<>();
        for (Map.Entry<VersionedKey, CacheKey> entry : asked.entrySet()) {
            V value = found.get(entry.getKey());
            if (value != null) {
                result.put(entry.getValue(), value);
            }
        }
        return result;
    }

    @Override
    public void put(CacheKey key, V value) {
        if (value != null) {
            cache.put(versioned(key), value);
        }
    }

    @Override
    public void evict(CacheKey key) {
        cache.invalidate(versioned(key));
    }

    @Override
    public void evictAll(Collection<CacheKey> keys) {
        cache.invalidateAll(keys.stream().map(this::versioned).toList());
    }

    @Override
    public void evictStore(StoreMerchantId store) {
        if (store != null) {
            versions.bump(store);
        }
    }

    @Override
    public void clear() {
        cache.invalidateAll();
        versions.clear();
    }

    @Override
    public RegionStats stats() {
        CacheStats stats = cache.stats();
        return new RegionStats(stats.hitCount(), stats.missCount(), stats.loadCount(), stats.evictionCount(),
                cache.estimatedSize());
    }

    private VersionedKey versioned(CacheKey key) {
        return new VersionedKey(key, versions.current(key.store()));
    }

    /** A key as stored: with its store's version when it was written. */
    record VersionedKey(CacheKey key, long version) {
    }
}
