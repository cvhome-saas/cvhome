package com.asrevo.cvhome.cache;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.cache.config.CacheProperties;
import com.asrevo.cvhome.cache.provider.CacheProvider;
import com.asrevo.cvhome.cache.provider.caffeine.CaffeineCacheProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The registry builds each declared region on the provider configuration names, with the configured
 * time-to-live and size over the declared ones, off when configuration says so, and refuses what it cannot build.
 */
class CacheRegistryTest {

    private static final String VALUE = "v";

    private static final List<CacheProvider> CAFFEINE = List.of(new CaffeineCacheProvider());

    private static final String CAFFEINE_NAME = "caffeine";

    private static final String PRODUCT = "test.product";

    private static final String LISTING = "test.listing";

    private static final String COUNTRY = "test.country";

    private static final String REDIS = "redis";

    private static final String TEST = "test";

    private static final String PRODUCT_READ = "product";

    @Test
    void declaredRegionsAreBuiltOnTheDefaultProvider() {
        CacheRegistry registry = new CacheRegistry(CacheRegions.of(TestRegions.values()), CAFFEINE,
                CacheProperties.defaults());

        assertThat(registry.names()).containsExactly(PRODUCT, LISTING, COUNTRY);
        assertThat(registry.regions()).hasSize(3);
        assertThat(registry.region(PRODUCT)).isPresent();
        assertThat(registry.region("test.none")).isEmpty();
        assertThat(registry.declaration(COUNTRY)).contains(TestRegions.COUNTRY);
        RegionCache<String> product = registry.region(TestRegions.PRODUCT, String.class);
        CacheKey key = CacheKey.of(Stores.A, Stores.EN);
        product.put(key, VALUE);
        assertThat(product.getIfPresent(key)).contains(VALUE);
    }

    @Test
    void configurationOverridesTtlSizeAndSwitchesARegionOff() {
        CacheProperties properties = new CacheProperties(CAFFEINE_NAME, Map.of(TEST, Map.of(
                PRODUCT_READ, new CacheProperties.Region(null, Duration.ofMillis(1), 1L, true),
                "listing", new CacheProperties.Region(null, null, null, false),
                "other", new CacheProperties.Region(null, null, null, true))));
        CacheRegistry registry = new CacheRegistry(CacheRegions.of(TestRegions.values()), CAFFEINE, properties);
        RegionCache<String> listing = registry.region(TestRegions.LISTING, String.class);
        CacheKey key = CacheKey.of(Stores.A, Stores.EN);

        listing.put(key, VALUE);

        assertThat(listing.getIfPresent(key)).as("switched off: nothing is held").isEmpty();
        assertThat(listing.stats().misses()).isEqualTo(1);
        assertThat(registry.region(TestRegions.PRODUCT, String.class).name()).isEqualTo(PRODUCT);
    }

    @Test
    void anUnknownProviderAnUndeclaredRegionAndTheWrongTypeAreRefused() {
        CacheProperties redis = new CacheProperties(CAFFEINE_NAME,
                Map.of(TEST, Map.of(PRODUCT_READ, new CacheProperties.Region(REDIS, null, null, true))));
        CacheRegions declared = CacheRegions.of(TestRegions.values());

        assertThatThrownBy(() -> new CacheRegistry(declared, CAFFEINE, redis))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining(REDIS);
        CacheRegistry registry = new CacheRegistry(CacheRegions.of(TestRegions.PRODUCT), CAFFEINE,
                CacheProperties.defaults());
        assertThatThrownBy(() -> registry.region(TestRegions.LISTING, String.class))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining(LISTING);
        assertThatThrownBy(() -> registry.region(TestRegions.PRODUCT, Integer.class))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("java.lang.String");
    }

    @Test
    void evictStoreReachesTheNamedRegionsOnly() {
        CacheRegistry registry = new CacheRegistry(CacheRegions.of(TestRegions.values()), CAFFEINE,
                CacheProperties.defaults());
        RegionCache<String> product = registry.region(TestRegions.PRODUCT, String.class);
        RegionCache<String> listing = registry.region(TestRegions.LISTING, String.class);
        CacheKey key = CacheKey.of(Stores.A, Stores.EN);
        product.put(key, VALUE);
        listing.put(key, VALUE);

        registry.evictStore(Stores.A, List.of(TestRegions.PRODUCT, TestRegions.COUNTRY));

        assertThat(product.getIfPresent(key)).isEmpty();
        assertThat(listing.getIfPresent(key)).contains(VALUE);
    }
}
