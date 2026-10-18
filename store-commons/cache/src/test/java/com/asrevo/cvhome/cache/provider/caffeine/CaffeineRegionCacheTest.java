package com.asrevo.cvhome.cache.provider.caffeine;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.cache.CacheKey;
import com.asrevo.cvhome.cache.RegionCache;
import com.asrevo.cvhome.cache.RegionStats;
import com.asrevo.cvhome.cache.Stores;
import com.asrevo.cvhome.cache.TestRegions;
import com.asrevo.cvhome.cache.provider.RegionSpec;
import com.asrevo.cvhome.commons.domain.Sku;
import com.github.benmanes.caffeine.cache.Ticker;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A region on Caffeine: a miss loads once, a null is never kept, a bulk read loads only what misses, a store's
 * eviction hides that store's entries and no other's, and an entry dies of age.
 */
class CaffeineRegionCacheTest {

    private static final String VALUE = "v";

    private static final String V1 = "v1";

    private static final Sku A1 = Sku.of("A1");

    private static final Sku A2 = Sku.of("A2");

    private static final Sku GONE = Sku.of("GONE");

    private long now;

    private RegionCache<String> cache;

    @BeforeEach
    void setUp() {
        Ticker ticker = () -> now;
        cache = new CaffeineCacheProvider(ticker).create(RegionSpec.of(TestRegions.PRODUCT));
        assertThat(new CaffeineCacheProvider().name()).isEqualTo("caffeine");
    }

    @Test
    void aMissLoadsOnceAndANullIsNotKept() {
        CacheKey key = CacheKey.of(Stores.A, Stores.EN);
        AtomicInteger loads = new AtomicInteger();

        assertThat(cache.get(key, k -> VALUE + loads.incrementAndGet())).isEqualTo(V1);
        assertThat(cache.get(key, k -> VALUE + loads.incrementAndGet())).isEqualTo(V1);
        assertThat(cache.getIfPresent(key)).contains(V1);
        assertThat(cache.name()).isEqualTo("test.product");

        CacheKey unknown = CacheKey.of(Stores.A, Stores.ES);
        assertThat(cache.get(unknown, k -> null)).isNull();
        assertThat(cache.getIfPresent(unknown)).isEmpty();
        cache.put(unknown, null);
        assertThat(cache.getIfPresent(unknown)).isEmpty();
        cache.put(unknown, VALUE);
        assertThat(cache.getIfPresent(unknown)).contains(VALUE);
    }

    @Test
    void aBulkReadLoadsOnlyWhatMissesAndLeavesTheUnknownOut() {
        CacheKey a1 = CacheKey.sku(Stores.A, Stores.EN, A1);
        CacheKey a2 = CacheKey.sku(Stores.A, Stores.EN, A2);
        CacheKey gone = CacheKey.sku(Stores.A, Stores.EN, GONE);
        List<Set<CacheKey>> asked = new ArrayList<>();

        Map<CacheKey, String> first = cache.getAll(List.of(a1, a2, a1, gone), keys -> {
            asked.add(keys);
            Map<CacheKey, String> loaded = new HashMap<>();
            keys.stream().filter(key -> key != gone).forEach(key -> loaded.put(key, key.render()));
            loaded.put(gone, null);
            return loaded;
        });
        Map<CacheKey, String> second = cache.getAll(List.of(a2, gone), keys -> {
            asked.add(keys);
            return Map.of();
        });

        assertThat(first).containsOnlyKeys(a1, a2);
        assertThat(second).containsOnlyKeys(a2);
        assertThat(asked).containsExactly(Set.of(a1, a2, gone), Set.of(gone));
        assertThat(cache.getAll(List.of(), keys -> Map.of())).isEmpty();
    }

    /**
     * The race the load stack found: a store write commits while a bulk read is loading, so the store's version moves
     * before the loaded rows are stored. Stamped with the new version they would be rows Caffeine does not return for
     * the keys asked, and a cart would be told its sku does not exist. They land under the version asked for, the
     * reader gets them, and the next reader loads again.
     */
    @Test
    void aBulkLoadThatCrossesAStoreEvictionStillAnswersAndIsNotKept() {
        CacheKey a1 = CacheKey.sku(Stores.A, Stores.EN, A1);
        CacheKey a2 = CacheKey.sku(Stores.A, Stores.EN, A2);
        List<Set<CacheKey>> asked = new ArrayList<>();

        Map<CacheKey, String> during = cache.getAll(List.of(a1, a2), keys -> {
            asked.add(keys);
            cache.evictStore(Stores.A);
            Map<CacheKey, String> loaded = new HashMap<>();
            keys.forEach(key -> loaded.put(key, key.render()));
            return loaded;
        });
        Map<CacheKey, String> after = cache.getAll(List.of(a1, a2), keys -> {
            asked.add(keys);
            Map<CacheKey, String> loaded = new HashMap<>();
            keys.forEach(key -> loaded.put(key, V1));
            return loaded;
        });

        assertThat(during).containsOnlyKeys(a1, a2);
        assertThat(after).containsEntry(a1, V1).containsEntry(a2, V1);
        assertThat(asked).as("the rows loaded across the eviction are not served after it").hasSize(2);
    }

    @Test
    void aStoresEvictionHidesItsEntriesAndNoOthers() {
        CacheKey a = CacheKey.of(Stores.A, Stores.EN);
        CacheKey b = CacheKey.of(Stores.B, Stores.EN);
        cache.put(a, VALUE);
        cache.put(b, VALUE);

        cache.evictStore(Stores.A);

        assertThat(cache.getIfPresent(a)).isEmpty();
        assertThat(cache.getIfPresent(b)).contains(VALUE);
        cache.put(a, VALUE);
        assertThat(cache.getIfPresent(a)).contains(VALUE);
        cache.evictStore(null);
        assertThat(cache.getIfPresent(a)).contains(VALUE);
        cache.evict(a);
        assertThat(cache.getIfPresent(a)).isEmpty();
        cache.evictAll(List.of(b));
        assertThat(cache.getIfPresent(b)).isEmpty();
    }

    @Test
    void anEntryDiesOfAgeAndClearForgetsEverything() {
        CacheKey key = CacheKey.of(Stores.A, Stores.EN);
        cache.put(key, VALUE);

        now += Duration.ofSeconds(59).toNanos();
        assertThat(cache.getIfPresent(key)).contains(VALUE);
        now += Duration.ofSeconds(2).toNanos();
        assertThat(cache.getIfPresent(key)).isEmpty();

        cache.put(key, VALUE);
        cache.evictStore(Stores.A);
        cache.clear();
        cache.put(key, VALUE);
        assertThat(cache.getIfPresent(key)).contains(VALUE);
        RegionStats stats = cache.stats();
        assertThat(stats.hits()).isPositive();
        assertThat(stats.misses()).isPositive();
        assertThat(stats.size()).isEqualTo(1);
        assertThat(RegionStats.none().size()).isZero();
    }
}
