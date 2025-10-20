package com.asrevo.cvhome.s2s.config.security;

import java.net.URI;
import java.util.Optional;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class KeycloakLogoutSuccessHandler {

	public static final String DEFAULT_LOGOUT_SUCCESS_URL = "/login?logout";

	private final ReactiveClientRegistrationRepository registrationRepository;

	private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

	private final URI logoutSuccessUrl = URI.create(DEFAULT_LOGOUT_SUCCESS_URL);

	public KeycloakLogoutSuccessHandler(ReactiveClientRegistrationRepository registrationRepository) {
		this.registrationRepository = registrationRepository;
	}

	private static String extractLogoutUrl(ServerWebExchange exchange,
			ClientRegistration.ProviderDetails providerDetails, String idToken) {
		ServerHttpRequest request = exchange.getRequest();
		String redirectPath = Optional.ofNullable(exchange.getRequest().getQueryParams().getFirst("redirect_path"))
			.orElse("");
		String idTokenHintParam = Optional.ofNullable(idToken).map(it -> "&id_token_hint=" + it).orElse("");
		String authorizationUri = providerDetails.getAuthorizationUri();
		String redirectUri = request.getURI().getScheme() + "://" + request.getURI().getAuthority() + "/"
				+ redirectPath;
		String logoutPath = "protocol/openid-connect/logout?post_logout_redirect_uri=" + redirectUri + idTokenHintParam;
		return authorizationUri.replace("protocol/openid-connect/auth", logoutPath);
	}

	public Mono<Void> onLogoutSuccess(ServerWebExchange exchange, Authentication authentication) {
		try {
			if (authentication instanceof OAuth2AuthenticationToken oAuth2AuthenticationToken) {
				ClientRegistration clientRegistration = registrationRepository
					.findByRegistrationId(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())
					.block();
				if (clientRegistration != null && clientRegistration.getProviderDetails() != null) {
					String idToken = null;
					if (oAuth2AuthenticationToken.getPrincipal() instanceof DefaultOidcUser defaultOidcUser) {
						idToken = defaultOidcUser.getIdToken().getTokenValue();
					}
					String logoutUrl = extractLogoutUrl(exchange, clientRegistration.getProviderDetails(), idToken);
					URI logoutSuccessUrl = URI.create(logoutUrl);
					return this.redirectStrategy.sendRedirect(exchange, logoutSuccessUrl);
				}
			}

		}
		catch (Exception ignored) {
		}
		return this.redirectStrategy.sendRedirect(exchange, logoutSuccessUrl);
	}

}
