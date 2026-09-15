package com.asrevo.cvhome.cache.provider;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.cache.CacheKey;
import com.asrevo.cvhome.cache.RegionCache;
import com.asrevo.cvhome.cache.Stores;
import com.asrevo.cvhome.cache.TestRegions;

import static org.assertj.core.api.Assertions.assertThat;

/** A region switched off: every read misses and loads, nothing is held, and the counters still move. */
class NoOpCacheProviderTest {

    private static final String VALUE = "v";

    @Test
    void everyReadMissesAndTheCountersMove() {
        NoOpCacheProvider provider = new NoOpCacheProvider();
        RegionCache<String> off = provider.create(RegionSpec.of(TestRegions.PRODUCT));
        CacheKey key = CacheKey.of(Stores.A, Stores.EN);
        AtomicInteger loads = new AtomicInteger();

        assertThat(provider.name()).isEqualTo("none");
        assertThat(off.name()).isEqualTo("test.product");
        assertThat(off.get(key, k -> VALUE + loads.incrementAndGet())).isEqualTo("v1");
        assertThat(off.get(key, k -> VALUE + loads.incrementAndGet())).isEqualTo("v2");
        off.put(key, VALUE);
        assertThat(off.getIfPresent(key)).isEmpty();
        assertThat(off.getAll(List.of(key, key), keys -> Map.of(key, VALUE))).containsEntry(key, VALUE);
        assertThat(off.getAll(List.of(), keys -> Map.of(key, VALUE))).isEmpty();
        off.evict(key);
        off.evictAll(List.of(key));
        off.evictStore(Stores.A);
        off.clear();
        assertThat(off.stats().misses()).isEqualTo(4);
        assertThat(off.stats().puts()).isEqualTo(1);
        assertThat(off.stats().hits()).isZero();
    }
}
