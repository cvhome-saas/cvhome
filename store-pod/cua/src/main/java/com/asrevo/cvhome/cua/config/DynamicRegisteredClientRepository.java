package com.asrevo.cvhome.cua.config;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class DynamicRegisteredClientRepository implements RegisteredClientRepository {

	@Override
	public void save(RegisteredClient registeredClient) {
		throw new UnsupportedOperationException("Saving registered clients is not supported");
	}

	@Override
	public @Nullable RegisteredClient findById(String id) {
		return createRegisteredClient(id);
	}

	@Override
	public @Nullable RegisteredClient findByClientId(String clientId) {
		return createRegisteredClient(clientId);
	}

	private RegisteredClient createRegisteredClient(String id) {
		String redirectUris = extractRedirectUri();

		return RegisteredClient.withId(id)
			.clientId(id)
			.clientIdIssuedAt(Instant.now())
			.clientName("Web App")
			.clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.redirectUris(it -> {
				if (Objects.nonNull(redirectUris)) {
					it.add(redirectUris);
				}
			})
			.scope(OidcScopes.OPENID)
			.clientSettings(ClientSettings.builder().requireProofKey(true).requireAuthorizationConsent(false).build())
			.tokenSettings(TokenSettings.builder()
				.reuseRefreshTokens(false)
				.accessTokenTimeToLive(Duration.ofSeconds(86400))
				.refreshTokenTimeToLive(Duration.ofSeconds(86400))
				.authorizationCodeTimeToLive(Duration.ofSeconds(86400))
				.deviceCodeTimeToLive(Duration.ofSeconds(300))
				.accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
				.build())
			.build();
	}

	private static String extractRedirectUri() {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attributes != null) {
			HttpServletRequest request = attributes.getRequest();
			String scheme = request.getScheme();
			String serverName = request.getServerName();
			int serverPort = request.getServerPort();

			StringBuilder dynamicUri = new StringBuilder();
			dynamicUri.append(scheme).append("://").append(serverName);
			if (("http".equals(scheme) && serverPort != 80) || ("https".equals(scheme) && serverPort != 443)) {
				dynamicUri.append(":").append(serverPort);
			}
			dynamicUri.append("/callback");
			return dynamicUri.toString();
		}
		return null;
	}

}
