package com.asrevo.cvhome.gateway.config;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.web.server.WebSession;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * After login the user lands on the {@code redirectTo} captured for this {@code state}, once, and only when it is a
 * same-site path. Anything else — no state, nothing captured, an absolute or scheme-relative URL — goes to {@code /}.
 */
class RedirectingServerAuthenticationSuccessHandlerTest {

    private static final String CALLBACK = "http://localhost/login/oauth2/code/uaa?code=c&state=%s";

    private static final String STATE = "st4te";

    private static final String REDIRECT_TO = "redirectTo";

    /**
     * Where a sign-in with nothing to resume lands. Not {@code /} — that is the public marketing page, which
     * still invites a signed-in person to sign in; the console routes on from {@code /dashboard} by itself.
     */
    private static final String CONSOLE_HOME = "/dashboard";

    private static final String SUB = "sub";

    private static final String DEEP_LINK = "/accept-invitation?token=abc";

    private static final String SESSION_KEY = "%s%s"
            .formatted(CapturingServerOAuth2AuthorizationRequestResolver.CAPTURED_PARAMETERS_SESSION_KEY_PREFIX, STATE);

    private final RedirectingServerAuthenticationSuccessHandler handler = new RedirectingServerAuthenticationSuccessHandler();

    private static Authentication oauth2Login() {
        DefaultOAuth2User user = new DefaultOAuth2User(null, Map.of(SUB, "u1"), SUB);
        return new OAuth2AuthenticationToken(user, null, "uaa");
    }

    private URI redirect(MockServerWebExchange exchange, Authentication authentication) {
        WebFilterExchange filterExchange = new WebFilterExchange(exchange, e -> Mono.empty());
        StepVerifier.create(handler.onAuthenticationSuccess(filterExchange, authentication)).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FOUND);
        return exchange.getResponse().getHeaders().getLocation();
    }

    private static MockServerWebExchange callbackWithCaptured(Map<String, String> captured) {
        MockServerWebExchange exchange = MockServerWebExchange
                .from(MockServerHttpRequest.get(CALLBACK.formatted(STATE)).build());
        WebSession session = exchange.getSession().block();
        session.getAttributes().put(SESSION_KEY, captured);
        return exchange;
    }

    @Test
    void missingStateFallsBackToTheConsoleHome() {
        MockServerWebExchange exchange = MockServerWebExchange
                .from(MockServerHttpRequest.get("http://localhost/login/oauth2/code/uaa?code=c").build());

        assertThat(redirect(exchange, oauth2Login())).hasToString(CONSOLE_HOME);
    }

    @Test
    void stateWithoutCapturedParametersFallsBackToTheConsoleHome() {
        MockServerWebExchange exchange = MockServerWebExchange
                .from(MockServerHttpRequest.get(CALLBACK.formatted("unknown")).build());

        assertThat(redirect(exchange, new TestingAuthenticationToken("u", "p"))).hasToString(CONSOLE_HOME);
    }

    @Test
    void capturedParametersWithoutRedirectToFallBackToRoot() {
        MockServerWebExchange exchange = callbackWithCaptured(new HashMap<>());

        assertThat(redirect(exchange, oauth2Login())).hasToString(CONSOLE_HOME);
    }

    @ParameterizedTest
    @ValueSource(strings = {"//evil.example/steal", "https://evil.example/steal", "http://[bad"})
    void offSiteOrMalformedRedirectFallsBackToTheConsoleHome(String redirectTo) {
        MockServerWebExchange exchange = callbackWithCaptured(Map.of(REDIRECT_TO, redirectTo));

        assertThat(redirect(exchange, oauth2Login())).hasToString(CONSOLE_HOME);
    }

    @Test
    void relativeRedirectIsHonouredAndConsumedFromTheSession() {
        MockServerWebExchange exchange = callbackWithCaptured(Map.of(REDIRECT_TO, DEEP_LINK));

        assertThat(redirect(exchange, oauth2Login())).hasToString(DEEP_LINK);
        assertThat(exchange.getSession().block().getAttributes()).doesNotContainKey(SESSION_KEY);
    }

}
