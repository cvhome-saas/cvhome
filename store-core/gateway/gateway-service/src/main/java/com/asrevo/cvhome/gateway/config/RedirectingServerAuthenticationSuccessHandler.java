package com.asrevo.cvhome.gateway.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
public class RedirectingServerAuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {

	private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

	private final URI defaultRedirectUri = URI.create("/");

	@Override
	public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
		ServerWebExchange exchange = webFilterExchange.getExchange();

		return exchange.getSession().flatMap(webSession -> {
			String stateFromCallback = exchange.getRequest().getQueryParams().getFirst("state");

			if (!StringUtils.hasText(stateFromCallback) && authentication instanceof OAuth2AuthenticationToken) {
				log.warn("State parameter not found directly in callback query"
						+ " params. This might affect redirectTo retrieval.");
			}

			if (StringUtils.hasText(stateFromCallback)) {
				String sessionKey = CapturingServerOAuth2AuthorizationRequestResolver.CAPTURED_PARAMETERS_SESSION_KEY_PREFIX
						+ stateFromCallback;
				Object capturedParamsObject = webSession.getAttributes().get(sessionKey);

				if (capturedParamsObject instanceof Map) {
					@SuppressWarnings("unchecked")
					Map<String, String> capturedParams = (Map<String, String>) capturedParamsObject;
					String redirectTo = capturedParams.get("redirectTo");

					log.info("Retrieved captured parameters from session (key: {}):" + " {}", sessionKey,
							capturedParams);
					// Remove from session after use
					webSession.getAttributes().remove(sessionKey);
					log.debug("Removed captured parameters from session with key: {}", sessionKey);

					if (StringUtils.hasText(redirectTo)) {
						if (isValidRedirect(redirectTo)) {
							log.info("Valid 'redirectTo' parameter found: '{}'." + " Redirecting.", redirectTo);
							return this.redirectStrategy.sendRedirect(exchange, URI.create(redirectTo));
						}
						else {
							log.warn(
									"Invalid or disallowed 'redirectTo' parameter:" + " '{}'. Falling back to default.",
									redirectTo);
						}
					}
					else {
						log.debug("'redirectTo' parameter not found in captured"
								+ " params or is empty. Falling back to" + " default.");
					}
				}
				else if (capturedParamsObject != null) {
					log.warn(
							"Captured parameters in session (key: {}) were not of"
									+ " expected Map type: {}. Falling back to" + " default.",
							sessionKey, capturedParamsObject.getClass().getName());
					webSession.getAttributes().remove(sessionKey); // Clean up mismatched
																	// type
				}
				else {
					log.debug("No captured parameters found in session for key: {}." + " Falling back to default.",
							sessionKey);
				}
			}
			else {
				log.warn("State parameter was missing or empty in OAuth2 callback."
						+ " Cannot retrieve 'redirectTo'. Falling back to" + " default.");
			}

			log.debug("Redirecting to default success URL: {}", defaultRedirectUri);
			return this.redirectStrategy.sendRedirect(exchange, defaultRedirectUri);
		});
	}

	private boolean isValidRedirect(String url) {
		if (!StringUtils.hasText(url)) {
			return false;
		}
		try {
			URI redirectUri = new URI(url);

			// Allow relative paths (which will be resolved against the app's base URL)
			if (!redirectUri.isAbsolute()) {
				// Check for potentially malicious relative paths like "//otherdomain.com"
				if (url.startsWith("//")) {
					log.warn("Disallowing scheme-relative URL that doesn't match app base: {}", url);
					return false;
				}
				// For truly relative paths like "/some/page" or "some/page"
				log.debug("Allowing relative redirect URI: {}", url);
				return true;
			}

			// If URL is not absolute and has no host (shouldn't happen with new URI(url)
			// if valid)
			return false;
		}
		catch (URISyntaxException e) {
			log.warn("Invalid URI syntax for redirectTo parameter: {}", url, e);
			return false;
		}
	}

}
