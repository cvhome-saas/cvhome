package com.asrevo.cvhome.testsupport.security;

import java.util.UUID;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;

/**
 * Replaces the multi-issuer {@code JwtDecoder} of a servlet service with one that trusts a fresh in-memory key, and
 * exposes the matching {@link TestJwtSigner}. Pulled in by {@code @ServiceIntegrationTest}.
 */
@TestConfiguration(proxyBeanMethods = false)
public class ServletTestSecurityConfiguration {

    @Bean
    RSAKey testRsaKey() throws JOSEException {
        return new RSAKeyGenerator(2048).keyUse(KeyUse.SIGNATURE).keyID(UUID.randomUUID().toString()).generate();
    }

    @Bean
    @Primary
    JwtDecoder testJwtDecoder(RSAKey rsaKey) throws JOSEException {
        return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
    }

    @Bean
    TestJwtSigner testJwtSigner(RSAKey rsaKey) throws JOSEException {
        return new TestJwtSigner(rsaKey);
    }

}
