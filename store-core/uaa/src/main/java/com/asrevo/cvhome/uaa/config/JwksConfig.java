package com.asrevo.cvhome.uaa.config;

import com.asrevo.cvhome.uaa.service.KeyPairService;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwksConfig {

	@Bean
	JWKSource<SecurityContext> jwkSource(KeyPairService keyPairService) {
		return (selector, context) -> selector.select(new JWKSet(keyPairService.getActiveAndPreviousKeys()));
	}

}
