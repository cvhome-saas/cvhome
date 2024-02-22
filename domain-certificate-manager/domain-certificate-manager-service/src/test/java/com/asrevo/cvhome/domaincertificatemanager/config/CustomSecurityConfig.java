package com.asrevo.cvhome.domaincertificatemanager.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import java.util.UUID;

@Configuration
public class CustomSecurityConfig {
    @Bean
    public RSAKey rsaKey() throws JOSEException {
        return new RSAKeyGenerator(2048)
                .keyUse(KeyUse.SIGNATURE) // indicate the intended use of the key (optional)
                .keyID(UUID.randomUUID().toString()) // give the key a unique ID (optional)
                .generate();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder(RSAKey rsaKey) throws JOSEException {
        return new NimbusReactiveJwtDecoder(rsaKey.toRSAPublicKey());
    }
}
