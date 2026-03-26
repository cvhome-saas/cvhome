package com.asrevo.cvhome.cua.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;


@Configuration
public class JwtCustomizerConfig {


	@Bean
	OAuth2TokenCustomizer<JwtEncodingContext> oauth2TokenCustomizer() {
		return context -> {
			// Only enrich access tokens
			if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
				return;
			}


		};
	}




}
