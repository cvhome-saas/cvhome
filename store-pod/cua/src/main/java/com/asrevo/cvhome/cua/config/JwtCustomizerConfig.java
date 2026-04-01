package com.asrevo.cvhome.cua.config;

import java.util.Map;
import java.util.Objects;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

@Configuration
public class JwtCustomizerConfig {

	private final static Map<String, String> attributeLookup = Map.of("email", "email", "name", "name", "given_name",
			"givenName", "family_name", "familyName", "picture", "picture", "avatar_url", "picture", "email_verified",
			"emailVerified");

	@Bean
	OAuth2TokenCustomizer<JwtEncodingContext> oauth2TokenCustomizer() {
		return context -> {

			if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
				return; // only modify access tokens
			}

			Map<String, Object> attrs = getOAuth2UserAttr(context);

			attrs.forEach((key, value) -> {
				if (attributeLookup.containsKey(key) && Objects.nonNull(value)) {
					String claimKey = attributeLookup.get(key);
					context.getClaims().claim(claimKey, value);
				}
			});

		};
	}

	private static Map<String, Object> getOAuth2UserAttr(JwtEncodingContext context) {
		Authentication rawPrincipal = context.getPrincipal();

		if (rawPrincipal instanceof OAuth2AuthenticatedPrincipal principal) {
			return principal.getAttributes();
		}
		else if (rawPrincipal instanceof OAuth2AuthenticationToken authToken
				&& Objects.nonNull(authToken.getPrincipal())) {
			return authToken.getPrincipal().getAttributes();
		}
		else if (rawPrincipal.getPrincipal() instanceof OAuth2AuthenticatedPrincipal principal) {
			return principal.getAttributes();
		}
		return Map.of();
	}

}
