package com.asrevo.cvhome.cua.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.cua.service.KeyPairService;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
public class JwksConfig {

    @Bean
    JWKSource<SecurityContext> jwkSource(KeyPairService keyPairService) {
        return (selector, context) -> selector.select(new JWKSet(keyPairService.getActiveAndPreviousKeys()));
    }

}
