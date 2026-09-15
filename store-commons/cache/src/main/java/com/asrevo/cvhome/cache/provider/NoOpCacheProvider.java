package com.asrevo.cvhome.cache.provider;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;

import com.asrevo.cvhome.cache.CacheKey;
import com.asrevo.cvhome.cache.RegionCache;
import com.asrevo.cvhome.cache.RegionStats;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * The provider of a region switched off in configuration: every read is a miss, every write is dropped, and the
 * meters still count, so turning a region off is visible on the dashboard rather than silent.
 */
public final class NoOpCacheProvider implements CacheProvider {

    public static final String NAME = "none";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public <V> RegionCache<V> create(RegionSpec spec) {
        return new Off<>(spec.name());
    }

    private static final class Off<V> implements RegionCache<V> {

        private final String name;

        private final LongAdder misses = new LongAdder();

        private final LongAdder puts = new LongAdder();

        private Off(String name) {
            this.name = name;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public Optional<V> getIfPresent(CacheKey key) {
            misses.increment();
            return Optional.empty();
        }

        @Override
        public V get(CacheKey key, Function<CacheKey, V> loader) {
            misses.increment();
            return loader.apply(key);
        }

        @Override
        public Map<CacheKey, V> getAll(Collection<CacheKey> keys, Function<Set<CacheKey>, Map<CacheKey, V>> loader) {
            Set<CacheKey> distinct = new LinkedHashSet<>(keys);
            misses.add(distinct.size());
            return distinct.isEmpty() ? Map.of() : new LinkedHashMap<>(loader.apply(distinct));
        }

        @Override
        public void put(CacheKey key, V value) {
            puts.increment();
        }

        @Override
        public void evict(CacheKey key) {
            // Nothing is held.
        }

        @Override
        public void evictAll(Collection<CacheKey> keys) {
            // Nothing is held.
        }

        @Override
        public void evictStore(StoreMerchantId store) {
            // Nothing is held.
        }

        @Override
        public void clear() {
            // Nothing is held.
        }

        @Override
        public RegionStats stats() {
            return new RegionStats(0, misses.sum(), puts.sum(), 0, 0);
        }
    }
}
