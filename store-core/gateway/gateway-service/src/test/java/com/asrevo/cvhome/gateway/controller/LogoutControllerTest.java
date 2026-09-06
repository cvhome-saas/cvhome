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

import com.asrevo.cvhome.gateway.impersonation.ImpersonationService;
import com.asrevo.cvhome.s2s.config.security.SecurityContextServerLogoutHandler;
import com.asrevo.cvhome.s2s.config.security.UaaLogoutSuccessHandler;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Logging out ends the gateway session and then sends the browser to uaa's end-session endpoint with the id token
 * hint, so the single sign-on session ends too; without a login there is nothing to end upstream.
 */
class LogoutControllerTest {

    private static final String END_SESSION = "http://uaa.gateway.com:8001/connect/logout";

    private static final String SUB = "sub";

    private static final String USER = "u1";

    private static final String UAA = "uaa";

    private final ImpersonationService impersonation = mock(ImpersonationService.class);

    private final LogoutController controller = new LogoutController(new UaaLogoutSuccessHandler(END_SESSION),
            new SecurityContextServerLogoutHandler(), impersonation);

    {
        when(impersonation.originalOf(any())).thenReturn(Mono.empty());
    }

    private static OAuth2AuthenticationToken oidcLogin(String idToken) {
        OidcIdToken token = new OidcIdToken(idToken, Instant.now(), Instant.now().plusSeconds(60), Map.of(SUB, USER));
        return new OAuth2AuthenticationToken(new DefaultOidcUser(null, token), null, UAA);
    }

    private static MockServerWebExchange logoutRequest() {
        return MockServerWebExchange.from(MockServerHttpRequest.get("http://gateway.com:8000/logout").build());
    }

    @Test
    void oidcLoginIsSentToUaaEndSessionWithTheIdTokenHint() {
        MockServerWebExchange exchange = logoutRequest();
        WebSession session = exchange.getSession().block();
        session.getAttributes().put("k", "v");
        session.start();
        OidcIdToken idToken = new OidcIdToken("id-token", Instant.now(), Instant.now().plusSeconds(60), Map.of(SUB, USER));
        OAuth2AuthenticationToken login = new OAuth2AuthenticationToken(new DefaultOidcUser(null, idToken), null, UAA);

        StepVerifier.create(controller.logout(exchange, login)).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FOUND);
        assertThat(exchange.getResponse().getHeaders().getFirst(HttpHeaders.LOCATION))
                .isEqualTo("%s?post_logout_redirect_uri=http://gateway.com:8000&id_token_hint=id-token".formatted(END_SESSION));
        assertThat(session.isExpired()).isTrue();
    }

    /**
     * Logging out while acting as a merchant ends the impersonation first: the swapped principal has no ID token,
     * so the end-session redirect must be built from the operator's own, or uaa's session would survive the logout.
     */
    @Test
    void logoutWhileImpersonatingEndsTheImpersonationAndSignsTheOperatorOut() {
        MockServerWebExchange exchange = logoutRequest();
        WebSession session = exchange.getSession().block();
        session.start();
        OAuth2AuthenticationToken operator = oidcLogin("operator-id-token");
        OAuth2AuthenticationToken merchant = new OAuth2AuthenticationToken(
                new org.springframework.security.oauth2.core.user.DefaultOAuth2User(null, Map.of(SUB, "m"), SUB), null, UAA);
        when(impersonation.originalOf(exchange)).thenReturn(Mono.just(operator));

        StepVerifier.create(controller.logout(exchange, merchant)).verifyComplete();

        assertThat(exchange.getResponse().getHeaders().getFirst(HttpHeaders.LOCATION))
                .endsWith("&id_token_hint=operator-id-token");
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
