package com.asrevo.cvhome.cache.provider;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.cache.CacheRegion;
import com.asrevo.cvhome.cache.TestRegions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegionSpecTest {

    private static final Duration MINUTE = Duration.ofMinutes(1);

    private static final String NAME = "x.y";

    @Test
    void aSpecCarriesTheDeclarationAndRefusesWhatCannotBeBuilt() {
        RegionSpec spec = RegionSpec.of(TestRegions.COUNTRY);

        assertThat(spec.name()).isEqualTo("test.country");
        assertThat(spec.scope()).isEqualTo(CacheRegion.Scope.GLOBAL);
        assertThat(spec.ttl()).isEqualTo(Duration.ofHours(1));
        assertThatThrownBy(() -> new RegionSpec(" ", String.class, MINUTE, 1, CacheRegion.Scope.STORE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RegionSpec(NAME, String.class, Duration.ZERO, 1, CacheRegion.Scope.STORE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RegionSpec(NAME, String.class, null, 1, CacheRegion.Scope.STORE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RegionSpec(NAME, String.class, MINUTE, 0, CacheRegion.Scope.STORE))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
