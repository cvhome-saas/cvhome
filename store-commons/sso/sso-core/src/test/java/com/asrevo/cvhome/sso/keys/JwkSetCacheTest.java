package com.asrevo.cvhome.sso.keys;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.nimbusds.jose.jwk.JWKSet;

import static org.assertj.core.api.Assertions.assertThat;

/** Loaded once, served from memory until invalidated. */
class JwkSetCacheTest {

    @Test
    void loadsOnceUntilInvalidated() {
        JwkSetCache cache = new JwkSetCache();
        AtomicInteger loads = new AtomicInteger();
        var loader = (java.util.function.Supplier<JWKSet>) () -> {
            loads.incrementAndGet();
            return new JWKSet(List.of());
        };

        JWKSet first = cache.get(loader);
        JWKSet second = cache.get(loader);
        cache.invalidate();
        JWKSet third = cache.get(loader);

        assertThat(first).isSameAs(second);
        assertThat(third).isNotSameAs(first);
        assertThat(loads.get()).isEqualTo(2);
    }

}
