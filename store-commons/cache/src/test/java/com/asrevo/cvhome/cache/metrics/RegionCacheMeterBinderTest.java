package com.asrevo.cvhome.cache.metrics;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.cache.CacheKey;
import com.asrevo.cvhome.cache.RegionCache;
import com.asrevo.cvhome.cache.Stores;
import com.asrevo.cvhome.cache.TestRegions;
import com.asrevo.cvhome.cache.provider.RegionSpec;
import com.asrevo.cvhome.cache.provider.caffeine.CaffeineCacheProvider;
import com.asrevo.cvhome.cache.spring.RegionSpringCache;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import static org.assertj.core.api.Assertions.assertThat;

/** A region reads on the dashboard like every other cache: the five standard meters, tagged by its name. */
class RegionCacheMeterBinderTest {

    private static final String CACHE = "cache";

    private static final String RESULT = "result";

    private static final String VALUE = "v";

    private static final String SERVICE = "service";

    private static final String TEST = "test";

    private static final String GETS = "cache.gets";

    private static final String PRODUCT = "test.product";

    @Test
    void theStandardCacheMetersCarryTheRegionsCounters() {
        RegionCache<Object> region = new CaffeineCacheProvider().create(RegionSpec.of(TestRegions.PRODUCT));
        RegionSpringCache cache = new RegionSpringCache(region);
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        CacheKey key = CacheKey.of(Stores.A, Stores.EN);
        region.get(key, k -> VALUE);
        region.get(key, k -> VALUE);
        region.getIfPresent(CacheKey.of(Stores.B, Stores.EN));

        new RegionCacheMeterBinderProvider().getMeterBinder(cache, List.of(Tag.of(SERVICE, TEST))).bindTo(registry);

        assertThat(registry.get(GETS).tag(CACHE, PRODUCT).tag(RESULT, "hit").functionCounter().count())
                .isEqualTo(1);
        assertThat(registry.get(GETS).tag(CACHE, PRODUCT).tag(RESULT, "miss").functionCounter().count())
                .isEqualTo(2);
        assertThat(registry.get("cache.puts").tag(CACHE, PRODUCT).functionCounter().count()).isEqualTo(1);
        assertThat(registry.get("cache.evictions").tag(CACHE, PRODUCT).functionCounter().count()).isZero();
        assertThat(registry.get("cache.size").tag(CACHE, PRODUCT).tag(SERVICE, TEST).gauge().value())
                .isEqualTo(1);
    }
}
