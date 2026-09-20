package com.asrevo.cvhome.cache.spring;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.cache.CacheRegions;
import com.asrevo.cvhome.cache.CacheRegistry;
import com.asrevo.cvhome.cache.TestRegions;
import com.asrevo.cvhome.cache.config.CacheProperties;
import com.asrevo.cvhome.cache.provider.caffeine.CaffeineCacheProvider;

import static org.assertj.core.api.Assertions.assertThat;

class CvhomeCacheManagerTest {

    private static final String PRODUCT = "test.product";

    @Test
    void aCachePerDeclaredRegionAndNoneForAnUndeclaredName() {
        CacheRegistry registry = new CacheRegistry(CacheRegions.of(TestRegions.values()),
                List.of(new CaffeineCacheProvider()), CacheProperties.defaults());
        CvhomeCacheManager manager = new CvhomeCacheManager(registry);

        assertThat(manager.getCacheNames()).containsExactly(PRODUCT, "test.listing", "test.country");
        assertThat(manager.getCache(PRODUCT)).isInstanceOf(RegionSpringCache.class);
        assertThat(manager.getCache("test.other")).isNull();
    }
}
