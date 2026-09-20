package com.asrevo.cvhome.cache.provider;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.cache.Stores;

import static org.assertj.core.api.Assertions.assertThat;

class StoreVersionsTest {

    @Test
    void eachStoreHasItsOwnCounter() {
        StoreVersions versions = new StoreVersions();

        assertThat(versions.current(Stores.A)).isZero();
        assertThat(versions.bump(Stores.A)).isEqualTo(1);
        assertThat(versions.bump(Stores.A)).isEqualTo(2);
        assertThat(versions.current(Stores.A)).isEqualTo(2);
        assertThat(versions.current(Stores.B)).isZero();
        versions.clear();
        assertThat(versions.current(Stores.A)).isZero();
    }
}
