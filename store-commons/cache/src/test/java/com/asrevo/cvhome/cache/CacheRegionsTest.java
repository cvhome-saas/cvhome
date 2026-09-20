package com.asrevo.cvhome.cache;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CacheRegionsTest {

    private static final String PRODUCT = "test.product";

    private static final String LISTING = "test.listing";

    private static final String COUNTRY = "test.country";

    @Test
    void regionsAreListedOnceByNameInDeclarationOrder() {
        CacheRegions regions = CacheRegions.of(TestRegions.values());

        assertThat(regions.names()).containsExactly(PRODUCT, LISTING, COUNTRY);
        assertThat(regions.byName(LISTING)).contains(TestRegions.LISTING);
        assertThat(regions.byName("test.none")).isEmpty();
        assertThat(CacheRegions.of(List.of(TestRegions.PRODUCT)).regions()).containsExactly(TestRegions.PRODUCT);
        assertThat(CacheRegions.none().regions()).isEmpty();
        assertThat(TestRegions.PRODUCT.scope()).isEqualTo(CacheRegion.Scope.STORE);
        assertThat(CacheRegions.merge(List.of(CacheRegions.of(TestRegions.PRODUCT), CacheRegions.of(TestRegions.COUNTRY)))
                .names()).containsExactly(PRODUCT, COUNTRY);
        assertThat(CacheRegions.merge(List.of()).regions()).isEmpty();
    }

    @Test
    void aNameDeclaredTwiceIsRefused() {
        assertThatThrownBy(() -> CacheRegions.of(TestRegions.PRODUCT, TestRegions.PRODUCT))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining(PRODUCT);
    }
}
