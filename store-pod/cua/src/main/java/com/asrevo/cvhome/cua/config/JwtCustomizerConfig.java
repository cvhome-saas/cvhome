package com.asrevo.cvhome.cua.config;

import com.asrevo.cvhome.cua.security.SecurityUser;
import java.util.Objects;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@Configuration
public class JwtCustomizerConfig {

	@Bean
	OAuth2TokenCustomizer<JwtEncodingContext> oauth2TokenCustomizer() {
		return context -> {

			if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
				return; // only modify access tokens
			}

			Authentication rawPrincipal = context.getPrincipal();

			if (rawPrincipal.getPrincipal() instanceof SecurityUser principal) {
				if (Objects.nonNull(principal.getEmail())) {
					context.getClaims().claim("email", principal.getEmail());
				}
				if (Objects.nonNull(principal.getFirstName())) {
					context.getClaims().claim("firstName", principal.getFirstName());
				}
				if (Objects.nonNull(principal.getLastName())) {
					context.getClaims().claim("lastName", principal.getLastName());
				}
				if (Objects.nonNull(principal.getClientId())) {
					context.getClaims().claim("clientId", principal.getClientId());
				}
				if (Objects.nonNull(principal.getId())) {
					context.getClaims().claim("sub", principal.getId().toString());
				}
				context.getClaims().claim("username", principal.getUsername());
			}

		};
	}

}
