package com.asrevo.cvhome.gateway.controller;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.web.server.WebSession;

import com.asrevo.cvhome.s2s.config.security.SecurityContextServerLogoutHandler;
import com.asrevo.cvhome.s2s.config.security.UaaLogoutSuccessHandler;

import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Logging out ends the gateway session and then sends the browser to uaa's end-session endpoint with the id token
 * hint, so the single sign-on session ends too; without a login there is nothing to end upstream.
 */
class LogoutControllerTest {

    private static final String END_SESSION = "http://uaa.gateway.com:8001/connect/logout";

    private final LogoutController controller = new LogoutController(new UaaLogoutSuccessHandler(END_SESSION),
            new SecurityContextServerLogoutHandler());

    private static MockServerWebExchange logoutRequest() {
        return MockServerWebExchange.from(MockServerHttpRequest.get("http://gateway.com:8000/logout").build());
    }

    @Test
    void oidcLoginIsSentToUaaEndSessionWithTheIdTokenHint() {
        MockServerWebExchange exchange = logoutRequest();
        WebSession session = exchange.getSession().block();
        session.getAttributes().put("k", "v");
        session.start();
        OidcIdToken idToken = new OidcIdToken("id-token", Instant.now(), Instant.now().plusSeconds(60), Map.of("sub", "u1"));
        OAuth2AuthenticationToken login = new OAuth2AuthenticationToken(new DefaultOidcUser(null, idToken), null, "uaa");

        StepVerifier.create(controller.logout(exchange, login)).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(exchange.getResponse().getHeaders().getFirst(HttpHeaders.LOCATION))
                .isEqualTo("%s?post_logout_redirect_uri=http://gateway.com:8000&id_token_hint=id-token".formatted(END_SESSION));
        assertThat(session.isExpired()).isTrue();
    }

    @Test
    void anonymousLogoutLandsOnTheLoginPage() {
        MockServerWebExchange exchange = logoutRequest();

        StepVerifier.create(controller.logout(exchange, null)).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(exchange.getResponse().getHeaders().getFirst(HttpHeaders.LOCATION)).isEqualTo("/login?logout");
    }

}
