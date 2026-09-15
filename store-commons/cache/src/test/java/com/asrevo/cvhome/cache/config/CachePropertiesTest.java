package com.asrevo.cvhome.cache.config;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

class CachePropertiesTest {

    private static final String CAFFEINE = "caffeine";

    private static final String X_Y = "x.y";

    private static final String REDIS = "redis";

    private static final String PRODUCT = "catalog.product";

    private static final String SUGGEST = "catalog.suggest";

    private static final String LISTING = "catalog.listing";

    @Test
    void theDefaultsAreCaffeineAndNoOverrides() {
        CacheProperties properties = CacheProperties.defaults();

        assertThat(properties.defaultProvider()).isEqualTo(CAFFEINE);
        assertThat(properties.region(X_Y).isEnabled()).isTrue();
        assertThat(properties.region(X_Y).ttl()).isNull();
        assertThat(new CacheProperties(CAFFEINE, null).region(X_Y).isEnabled()).isTrue();
        assertThat(new CacheProperties.Region(null, null, null, null).isEnabled()).isTrue();
    }

    @Test
    void aRegionBindsFromItsDottedName() {
        Binder binder = new Binder(new MapConfigurationPropertySource(Map.of(
                "com.asrevo.cvhome.cache.default-provider", CAFFEINE,
                "com.asrevo.cvhome.cache.regions.catalog.product.ttl", "30s",
                "com.asrevo.cvhome.cache.regions.catalog.product.max-size", "5",
                "com.asrevo.cvhome.cache.regions.catalog.suggest.enabled", "false",
                "com.asrevo.cvhome.cache.regions.catalog.listing.provider", REDIS)));

        CacheProperties properties = binder.bind("com.asrevo.cvhome.cache", CacheProperties.class).get();

        assertThat(properties.region(PRODUCT).ttl()).isEqualTo(Duration.ofSeconds(30));
        assertThat(properties.region(PRODUCT).maxSize()).isEqualTo(5);
        assertThat(properties.region(PRODUCT).isEnabled()).isTrue();
        assertThat(properties.region(SUGGEST).isEnabled()).isFalse();
        assertThat(properties.region(LISTING).provider()).isEqualTo(REDIS);
        assertThat(properties.configuredNames()).containsExactlyInAnyOrder(PRODUCT, SUGGEST, LISTING);
        assertThat(properties.region("nodot").isEnabled()).isTrue();
        assertThat(properties.region("other.read").ttl()).isNull();
        assertThat(CacheProperties.defaults().configuredNames()).isEmpty();
        assertThat(new CacheProperties(CAFFEINE, null).configuredNames()).isEmpty();
    }
}
