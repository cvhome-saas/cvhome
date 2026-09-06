package com.asrevo.cvhome.checkout.config;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.asrevo.cvhome.checkout.domain.ShopperId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Only a shopper token yields a {@link ShopperId}; staff, services and anonymous callers resolve to null.
 */
class ShopperArgumentResolverTest {

    private static final String ROLE_CUSTOMER_2 = "ROLE_CUSTOMER";

    private static final String ACC_1 = "acc-1";

    private static final String ACC_2 = "acc-2";

    private final ShopperArgumentResolver resolver = new ShopperArgumentResolver();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private static JwtAuthenticationToken jwt(String sub, String... roles) {
        Jwt token = new Jwt("t", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "none"),
                Map.of("sub", sub, "roles", List.of(roles)));
        return new JwtAuthenticationToken(token, List.of(roles).stream().map(SimpleGrantedAuthority::new).toList());
    }

    @Test
    void aShopperTokenResolvesToItsSubject() {
        assertThat(ShopperArgumentResolver.resolve(jwt(ACC_1, ROLE_CUSTOMER_2))).isEqualTo(new ShopperId(ACC_1));
    }

    @Test
    void anythingElseResolvesToNull() {
        assertThat(ShopperArgumentResolver.resolve(jwt("admin@store", "ROLE_STORE_ADMIN"))).isNull();
        assertThat(ShopperArgumentResolver.resolve(jwt("s2s", "SCOPE_store_pod"))).isNull();
        assertThat(ShopperArgumentResolver.resolve(null)).isNull();
        assertThat(ShopperArgumentResolver.resolve(new UsernamePasswordAuthenticationToken("u", "p"))).isNull();
        assertThat(ShopperArgumentResolver.resolve(jwt(" ", ROLE_CUSTOMER_2))).isNull();
    }

    @Test
    void supportsOnlyAnnotatedShopperIdParametersAndReadsTheSecurityContext() throws Exception {
        MethodParameter annotated = new MethodParameter(Sample.class.getMethod("annotated", ShopperId.class), 0);
        MethodParameter plain = new MethodParameter(Sample.class.getMethod("plain", ShopperId.class), 0);
        MethodParameter wrongType = new MethodParameter(Sample.class.getMethod("wrongType", String.class), 0);

        assertThat(resolver.supportsParameter(annotated)).isTrue();
        assertThat(resolver.supportsParameter(plain)).isFalse();
        assertThat(resolver.supportsParameter(wrongType)).isFalse();

        SecurityContextHolder.getContext().setAuthentication(jwt(ACC_2, ROLE_CUSTOMER_2));
        assertThat(resolver.resolveArgument(annotated, null, null, null)).isEqualTo(new ShopperId(ACC_2));
    }

    static final class Sample {

        public void annotated(@CurrentShopper ShopperId shopper) {
        }

        public void plain(ShopperId shopper) {
        }

        public void wrongType(@CurrentShopper String shopper) {
        }
    }
}
