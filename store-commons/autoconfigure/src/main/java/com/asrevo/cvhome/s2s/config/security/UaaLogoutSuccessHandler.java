package com.asrevo.cvhome.s2s.config.security;

import java.net.URI;
import java.util.Optional;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class UaaLogoutSuccessHandler {

	public static final String DEFAULT_LOGOUT_SUCCESS_URL = "/login?logout";

	private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

	private final URI logoutSuccessUrl = URI.create(DEFAULT_LOGOUT_SUCCESS_URL);

	private final String endSessionEndpoint;

	public UaaLogoutSuccessHandler(String endSessionEndpoint) {
		this.endSessionEndpoint = endSessionEndpoint;
	}

	private String extractLogoutUrl(ServerWebExchange exchange, String idToken) {
		ServerHttpRequest request = exchange.getRequest();
		String idTokenHintParam = Optional.ofNullable(idToken).map(it -> "&id_token_hint=" + it).orElse("");
		String redirectUri = request.getURI().getScheme() + "://" + request.getURI().getAuthority();
		return this.endSessionEndpoint + "?post_logout_redirect_uri=" + redirectUri + idTokenHintParam;
	}

	public Mono<Void> onLogoutSuccess(ServerWebExchange exchange, Authentication authentication) {
		try {
			if (authentication instanceof OAuth2AuthenticationToken oAuth2AuthenticationToken) {

				String idToken = null;
				if (oAuth2AuthenticationToken.getPrincipal() instanceof DefaultOidcUser defaultOidcUser) {
					idToken = defaultOidcUser.getIdToken().getTokenValue();
				}
				String logoutUrl = extractLogoutUrl(exchange, idToken);
				URI logoutSuccessUrl = URI.create(logoutUrl);
				return this.redirectStrategy.sendRedirect(exchange, logoutSuccessUrl);
			}

		}
		catch (Exception ignored) {
		}
		return this.redirectStrategy.sendRedirect(exchange, logoutSuccessUrl);
	}

}
