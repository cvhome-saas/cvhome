package com.asrevo.cvhome.gateway.controller;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.server.ServerWebExchange;

import com.asrevo.cvhome.s2s.config.security.UaaLogoutSuccessHandler;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Where the browser is sent to end uaa's session — which is not always uaa's own address.
 *
 * <p>
 * This gateway serves uaa under {@code /uaa} on its own origin, so uaa's session cookie belongs to that origin and
 * that path. An end-session call sent to uaa's own host carries no cookie: this session ends, uaa's does not, and
 * the next navigation is signed straight back in without a password — which reads as "logging out put me back on
 * the page I just left". Tested here rather than beside the class because the gateway is its only caller and the
 * one module with reactor on its test classpath.
 * </p>
 */
class UaaLogoutSuccessHandlerTest {

    private static final String END_SESSION = "http://uaa.gateway.com:8001/connect/logout";

    private static final String CONSOLE = "http://gateway.com:8000/logout";

    private static final String PREFIX = "/uaa";

    private static final String SUB = "sub";

    private static ServerWebExchange from(String url) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(url).build());
    }

    private static String logoutTo(UaaLogoutSuccessHandler handler, ServerWebExchange exchange, Authentication who) {
        handler.onLogoutSuccess(exchange, who).block();
        return exchange.getResponse().getHeaders().getFirst(HttpHeaders.LOCATION);
    }

    /** Somebody who signed in through uaa. Not an OIDC user, so there is no `id_token_hint` to carry. */
    private static Authentication signedInThroughUaa() {
        DefaultOAuth2User user = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority("ROLE_ORG_ADMIN")),
                Map.of(SUB, "318f2fd5"), SUB);
        return new OAuth2AuthenticationToken(user, user.getAuthorities(), "uaa");
    }

    /**
     * A principal that did not come from uaa has no session there to end, so there is only a page to land on.
     *
     * <p>
     * This is also the only branch reachable with a plain {@code Authentication}: the rewrite below applies to the
     * end-session redirect, which is built solely for an {@code OAuth2AuthenticationToken}.
     * </p>
     */
    /** The one that matters: the call goes to this origin, under the forward path, not to uaa's own host. */
    @Test
    void aSameOriginPrefixMovesTheEndSessionCallOntoTheRequestsOwnOrigin() {
        String location = logoutTo(new UaaLogoutSuccessHandler(END_SESSION, PREFIX), from(CONSOLE),
                signedInThroughUaa());

        assertThat(location).startsWith("http://gateway.com:8000/uaa/connect/logout?post_logout_redirect_uri=")
                .doesNotContain("uaa.gateway.com");
    }

    /**
     * The console answers on several hosts, so the origin is the request's own: the cookie to be cleared belongs
     * to whichever one the person is on, and a configured host would clear the wrong one — or none.
     */
    @Test
    void theOriginIsTheRequestsOwnRatherThanAConfiguredHost() {
        String location = logoutTo(new UaaLogoutSuccessHandler(END_SESSION, PREFIX),
                from("http://console-ui.gateway.com:8000/logout"), signedInThroughUaa());

        assertThat(location).startsWith("http://console-ui.gateway.com:8000/uaa/connect/logout");
    }

    /** Unconfigured, the handler is exactly what it was before uaa moved behind this gateway. */
    @Test
    void withoutASameOriginPrefixTheConfiguredEndpointIsUsedAsIs() {
        String location = logoutTo(new UaaLogoutSuccessHandler(END_SESSION), from(CONSOLE), signedInThroughUaa());

        assertThat(location).startsWith(String.format("%s?post_logout_redirect_uri=", END_SESSION));
    }

    /** A principal that did not come from uaa has no session there to end, so there is only a page to land on. */
    @Test
    void aNonOauthPrincipalJustGoesToTheLoginPage() {
        assertThat(logoutTo(new UaaLogoutSuccessHandler(END_SESSION, PREFIX), from(CONSOLE),
                new TestingAuthenticationToken("someone", "secret")))
                .isEqualTo(UaaLogoutSuccessHandler.DEFAULT_LOGOUT_SUCCESS_URL);
    }

}
