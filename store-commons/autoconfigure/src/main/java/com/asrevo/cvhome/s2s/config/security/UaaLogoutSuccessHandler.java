package com.asrevo.cvhome.s2s.config.security;

import java.net.URI;
import java.util.Optional;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;

import reactor.core.publisher.Mono;

/**
 * Ends the session at uaa as well as here, and does it where the browser actually holds one.
 *
 * <p>
 * {@code sameOriginPrefix} is what makes that true when uaa is served under a path on this gateway's own origin.
 * uaa's session cookie is then scoped to that origin and that path, so sending the browser to uaa's own host to
 * log out presents no cookie at all: this session ends, uaa's does not, and the next navigation is signed straight
 * back in without a password — a logout that visibly returns the person to the page they just left. Rebuilding the
 * end-session URL on the request's own origin is what actually ends it.
 * </p>
 */
public class UaaLogoutSuccessHandler {

    public static final String DEFAULT_LOGOUT_SUCCESS_URL = "/login?logout";

    private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

    private final URI logoutSuccessUrl = URI.create(DEFAULT_LOGOUT_SUCCESS_URL);

    private final String endSessionEndpoint;

    /** Path uaa is served under on this gateway's own origin, or empty when uaa is reached at its own address. */
    private final String sameOriginPrefix;

    public UaaLogoutSuccessHandler(String endSessionEndpoint) {
        this(endSessionEndpoint, "");
    }

    public UaaLogoutSuccessHandler(String endSessionEndpoint, String sameOriginPrefix) {
        this.endSessionEndpoint = endSessionEndpoint;
        this.sameOriginPrefix = sameOriginPrefix == null ? "" : sameOriginPrefix;
    }

    /**
     * The configured endpoint, moved onto the origin this request came in on.
     *
     * <p>
     * Only the address changes; the path uaa answers on is the configured one, with the gateway's forward prefix
     * in front of it. The origin comes from the request because the console answers on several hosts and the
     * cookie to be cleared belongs to whichever one the person is on.
     * </p>
     */
    private String endSessionFor(ServerHttpRequest request) {
        if (!StringUtils.hasText(sameOriginPrefix)) {
            return endSessionEndpoint;
        }
        String path = UriComponentsBuilder.fromUriString(endSessionEndpoint).build().getPath();
        return UriComponentsBuilder.fromUriString(endSessionEndpoint)
                .scheme(request.getURI().getScheme())
                .host(request.getURI().getHost())
                .port(request.getURI().getPort())
                .replacePath(String.format("%s%s", sameOriginPrefix, path))
                .build()
                .toUriString();
    }

    private String extractLogoutUrl(ServerWebExchange exchange, String idToken) {
        ServerHttpRequest request = exchange.getRequest();
        String idTokenHintParam = Optional.ofNullable(idToken)
                .map(it -> String.format("&id_token_hint=%s", it))
                .orElse("");
        String redirectUri = String.format("%s://%s", request.getURI().getScheme(), request.getURI().getAuthority());
        return String.format("%s?post_logout_redirect_uri=%s%s", endSessionFor(request), redirectUri,
                idTokenHintParam);
    }

    public Mono<Void> onLogoutSuccess(ServerWebExchange exchange, Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken oAuth2AuthenticationToken) {

            String idToken = null;
            if (oAuth2AuthenticationToken.getPrincipal() instanceof DefaultOidcUser defaultOidcUser) {
                idToken = defaultOidcUser.getIdToken().getTokenValue();
            }
            String logoutUrl = extractLogoutUrl(exchange, idToken);
            return this.redirectStrategy.sendRedirect(exchange, URI.create(logoutUrl));
        }
        return this.redirectStrategy.sendRedirect(exchange, logoutSuccessUrl);
    }

}
