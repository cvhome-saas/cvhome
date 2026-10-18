package com.asrevo.cvhome.catalog.reads;

import java.time.Duration;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.cache.CacheRegion;
import com.asrevo.cvhome.cache.CacheRegions;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogRegionsTest {

    @Test
    void everyRegionIsNamedForTheServiceHeldAMinuteAndScopedByStore() {
        CacheRegions regions = CacheRegions.of(CatalogRegions.values());

        assertThat(regions.names()).hasSize(11).allMatch(name -> name.startsWith("catalog."));
        assertThat(Arrays.stream(CatalogRegions.values())).allSatisfy(region -> {
            assertThat(region.ttl()).isEqualTo(Duration.ofSeconds(60));
            assertThat(region.maxSize()).isPositive();
            assertThat(region.valueType()).isNotNull();
            assertThat(region.scope()).isEqualTo(CacheRegion.Scope.STORE);
        });
        assertThat(CatalogRegions.CART_LINE.regionName()).isEqualTo(CatalogRegions.Names.CART_LINE)
                .isEqualTo("catalog.cart-line");
    }
}
