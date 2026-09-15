package com.asrevo.cvhome.content.reads;

import java.time.Duration;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.cache.CacheRegion;
import com.asrevo.cvhome.cache.CacheRegions;

import static org.assertj.core.api.Assertions.assertThat;

class ContentRegionsTest {

    @Test
    void everyRegionIsNamedForTheServiceScopedByStoreAndTheFrontPageOnesAreShortLived() {
        assertThat(CacheRegions.of(ContentRegions.values()).names()).hasSize(11)
                .allMatch(name -> name.startsWith("content."));
        assertThat(Arrays.stream(ContentRegions.values())).allSatisfy(region -> {
            assertThat(region.scope()).isEqualTo(CacheRegion.Scope.STORE);
            assertThat(region.maxSize()).isPositive();
            assertThat(region.valueType()).isNotNull();
        });
        assertThat(ContentRegions.SITE.ttl()).isEqualTo(Duration.ofSeconds(10));
        assertThat(ContentRegions.PAGE.ttl()).isEqualTo(Duration.ofSeconds(60));
        assertThat(ContentRegions.POST_CATEGORIES.regionName()).isEqualTo("content.post-categories");
    }
}
