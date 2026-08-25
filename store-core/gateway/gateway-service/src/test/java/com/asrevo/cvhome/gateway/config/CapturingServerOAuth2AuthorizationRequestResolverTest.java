package com.asrevo.cvhome.gateway.config;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the 500 on every deep link with a query string: {@code redirectTo} is forwarded to the authorization server
 * re-encoded, and remembered in the session under the request's {@code state} so the callback can honour it.
 */
class CapturingServerOAuth2AuthorizationRequestResolverTest {

    private static final String LOGIN = "http://localhost:8000/oauth2/authorization/uaa";

    private static final String REDIRECT_TO = "redirectTo";

    private static final String DEEP_LINK = "/accept-invitation?token=abc";

    private static final String UAA = "uaa";

    private static final String CLIENT_ID = "client_id=web-app";

    private final CapturingServerOAuth2AuthorizationRequestResolver resolver =
            new CapturingServerOAuth2AuthorizationRequestResolver(new InMemoryReactiveClientRegistrationRepository(
                    ClientRegistration.withRegistrationId(UAA)
                            .clientId("web-app")
                            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                            .authorizationUri("http://uaa/oauth2/authorize")
                            .tokenUri("http://uaa/oauth2/token")
                            .scope("openid")
                            .build()));

    private static MockServerWebExchange login(String redirectTo) {
        String url = redirectTo == null ? LOGIN
                : "%s?%s=%s".formatted(LOGIN, REDIRECT_TO, UriUtils.encodeQueryParam(redirectTo, StandardCharsets.UTF_8));
        return MockServerWebExchange.from(MockServerHttpRequest.method(HttpMethod.GET, URI.create(url)).build());
    }

    private static String forwardedRedirectTo(OAuth2AuthorizationRequest request) {
        String encoded = UriComponentsBuilder.fromUriString(request.getAuthorizationRequestUri())
                .build()
                .getQueryParams()
                .getFirst(REDIRECT_TO);
        return encoded == null ? null : UriUtils.decode(encoded, StandardCharsets.UTF_8);
    }

    @Test
    void deepLinkWithAQueryStringIsForwardedAndRemembered() {
        MockServerWebExchange exchange = login(DEEP_LINK);

        OAuth2AuthorizationRequest request = resolver.resolve(exchange).block();

        assertThat(request).isNotNull();
        assertThat(forwardedRedirectTo(request)).isEqualTo(DEEP_LINK);
        assertThat(request.getAuthorizationRequestUri()).contains(CLIENT_ID);
        Object captured = exchange.getSession().block().getAttributes().get("%s%s".formatted(
                CapturingServerOAuth2AuthorizationRequestResolver.CAPTURED_PARAMETERS_SESSION_KEY_PREFIX,
                request.getState()));
        assertThat(captured).isEqualTo(Map.of(REDIRECT_TO, DEEP_LINK));
    }

    @Test
    void loginWithoutParametersLeavesTheRequestAndSessionAlone() {
        MockServerWebExchange exchange = login(null);

        OAuth2AuthorizationRequest request = resolver.resolve(exchange, UAA).block();

        assertThat(request).isNotNull();
        assertThat(forwardedRedirectTo(request)).isNull();
        assertThat(exchange.getSession().block().getAttributes()).isEmpty();
    }

    @Test
    void existingAuthorizationParametersAreNeverOverridden() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.method(HttpMethod.GET,
                URI.create("%s?client_id=spoofed&%s=%%2Fdashboard".formatted(LOGIN, REDIRECT_TO))).build());

        OAuth2AuthorizationRequest request = resolver.resolve(exchange, UAA).block();

        assertThat(request.getAuthorizationRequestUri()).contains(CLIENT_ID).doesNotContain("spoofed");
        assertThat(forwardedRedirectTo(request)).isEqualTo("/dashboard");
    }

    @Test
    void unrelatedPathResolvesToNothing() {
        MockServerWebExchange exchange = MockServerWebExchange
                .from(MockServerHttpRequest.get("http://localhost:8000/dashboard").build());

        StepVerifier.create(resolver.resolve(exchange)).verifyComplete();
        StepVerifier.create(resolver.resolve(exchange, "unknown")).verifyError(ResponseStatusException.class);
    }

}
