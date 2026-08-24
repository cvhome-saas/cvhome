package com.asrevo.cvhome.testsupport.security;

import java.util.UUID;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;

/**
 * The WebFlux twin of {@link ServletTestSecurityConfiguration}, for the gateway.
 */
@TestConfiguration(proxyBeanMethods = false)
public class ReactiveTestSecurityConfiguration {

    @Bean
    RSAKey testRsaKey() throws JOSEException {
        return new RSAKeyGenerator(2048).keyUse(KeyUse.SIGNATURE).keyID(UUID.randomUUID().toString()).generate();
    }

    @Bean
    @Primary
    ReactiveJwtDecoder testReactiveJwtDecoder(RSAKey rsaKey) throws JOSEException {
        return new NimbusReactiveJwtDecoder(rsaKey.toRSAPublicKey());
    }

    @Bean
    TestJwtSigner testJwtSigner(RSAKey rsaKey) throws JOSEException {
        return new TestJwtSigner(rsaKey);
    }

}
