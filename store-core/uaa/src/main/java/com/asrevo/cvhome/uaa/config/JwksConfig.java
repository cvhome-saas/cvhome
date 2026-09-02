package com.asrevo.cvhome.uaa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.uaa.keys.JwkSetCache;
import com.asrevo.cvhome.uaa.keys.KeyRotationService;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

/**
 * The JWK source behind the encoder, the decoder and the JWKS endpoint: the cached set from {@link KeyRotationService}.
 * The encoder picks its key by the {@code kid} the token customizer stamps on the header, so an active key beside a
 * retiring one is not "multiple signing keys" to it.
 */
@Configuration
public class JwksConfig {

    @Bean
    JWKSource<SecurityContext> jwkSource(KeyRotationService keys, JwkSetCache cache) {
        return (selector, context) -> selector.select(cache.get(keys::currentJwkSet));
    }

}
