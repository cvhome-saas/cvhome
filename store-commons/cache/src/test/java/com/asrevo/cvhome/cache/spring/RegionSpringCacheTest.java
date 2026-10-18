package com.asrevo.cvhome.cache.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;

import com.asrevo.cvhome.cache.CacheKey;
import com.asrevo.cvhome.cache.RegionCache;
import com.asrevo.cvhome.cache.Stores;
import com.asrevo.cvhome.cache.TestRegions;
import com.asrevo.cvhome.cache.provider.RegionSpec;
import com.asrevo.cvhome.cache.provider.caffeine.CaffeineCacheProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegionSpringCacheTest {

    private static final String VALUE = "v";

    private static final CacheKey KEY = CacheKey.of(Stores.A, Stores.EN);

    private RegionSpringCache cache;

    @BeforeEach
    void setUp() {
        RegionCache<Object> region = new CaffeineCacheProvider().create(RegionSpec.of(TestRegions.PRODUCT));
        cache = new RegionSpringCache(region);
        assertThat(cache.region()).isSameAs(region);
    }

    @Test
    void readsAndWritesGoThroughTheRegion() {
        assertThat(cache.getName()).isEqualTo("test.product");
        assertThat(cache.getNativeCache()).isInstanceOf(RegionCache.class);
        assertThat(cache.get(KEY)).isNull();
        cache.put(KEY, VALUE);
        assertThat(cache.get(KEY).get()).isEqualTo(VALUE);
        assertThat(cache.get(KEY, String.class)).isEqualTo(VALUE);
        assertThat(cache.get(KEY, (Class<Object>) null)).isEqualTo(VALUE);
        assertThatThrownBy(() -> cache.get(KEY, Integer.class)).isInstanceOf(IllegalStateException.class);
        cache.evict(KEY);
        assertThat(cache.get(KEY, String.class)).isNull();
        cache.put(KEY, null);
        assertThat(cache.get(KEY)).isNull();
        cache.put(KEY, VALUE);
        cache.clear();
        assertThat(cache.get(KEY)).isNull();
    }

    @Test
    void aLoaderRunsOnceAndItsFailureIsReported() {
        assertThat(cache.get(KEY, () -> VALUE)).isEqualTo(VALUE);
        assertThat(cache.get(KEY, () -> "other")).isEqualTo(VALUE);
        CacheKey failing = CacheKey.of(Stores.B, Stores.EN);
        assertThatThrownBy(() -> cache.get(failing, () -> {
            throw new IllegalStateException("db down");
        })).isInstanceOf(Cache.ValueRetrievalException.class);
    }

    @Test
    void aKeyThatIsNotACacheKeyIsAWiringMistake() {
        assertThatThrownBy(() -> cache.get("plain")).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("java.lang.String");
        assertThatThrownBy(() -> cache.put(null, VALUE)).isInstanceOf(IllegalArgumentException.class);
    }
}
