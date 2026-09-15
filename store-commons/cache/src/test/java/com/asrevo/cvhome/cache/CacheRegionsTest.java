package com.asrevo.cvhome.cache;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CacheRegionsTest {

    private static final String PRODUCT = "test.product";

    private static final String LISTING = "test.listing";

    @Test
    void regionsAreListedOnceByNameInDeclarationOrder() {
        CacheRegions regions = CacheRegions.of(TestRegions.values());

        assertThat(regions.names()).containsExactly(PRODUCT, LISTING, "test.country");
        assertThat(regions.byName(LISTING)).contains(TestRegions.LISTING);
        assertThat(regions.byName("test.none")).isEmpty();
        assertThat(CacheRegions.of(List.of(TestRegions.PRODUCT)).regions()).containsExactly(TestRegions.PRODUCT);
        assertThat(CacheRegions.none().regions()).isEmpty();
        assertThat(TestRegions.PRODUCT.scope()).isEqualTo(CacheRegion.Scope.STORE);
    }

    @Test
    void aNameDeclaredTwiceIsRefused() {
        assertThatThrownBy(() -> CacheRegions.of(TestRegions.PRODUCT, TestRegions.PRODUCT))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining(PRODUCT);
    }
}
