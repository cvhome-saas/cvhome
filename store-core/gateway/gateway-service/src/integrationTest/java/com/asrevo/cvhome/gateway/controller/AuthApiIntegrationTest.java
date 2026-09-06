package com.asrevo.cvhome.gateway.controller;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import com.asrevo.cvhome.billing.services.entitlement.ReactiveExternalEntitlementService;
import com.asrevo.cvhome.podregistry.api.ReactiveExternalPodService;
import com.asrevo.cvhome.testsupport.annotations.ReactiveIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The login round trip's edges over real HTTP: starting a login forwards the deep link to uaa and opens a gateway
 * session; the session endpoints refuse an anonymous caller; logout without a login lands on the login page.
 */
@ReactiveIntegrationTest
class AuthApiIntegrationTest {

    private static final String DEEP_LINK = "/accept-invitation?token=abc";

    @LocalServerPort
    private int port;

    @MockitoBean
    private ReactiveExternalPodService podService;

    @MockitoBean
    private ReactiveExternalEntitlementService entitlementService;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:%d".formatted(port)).build();
    }

    /**
     * The authorize request goes to uaa <em>on this gateway's own origin</em>, not to uaa's address.
     *
     * <p>
     * The console renders the sign-in page now, so the visible half of the flow has to stay on one origin: uaa's
     * session cookie is scoped to it and is what carries the saved authorize request across the form POST, and the
     * console can only read the CSRF cookie if it was set there. Cross-origin the flow still authenticates and then
     * restarts instead of resuming, which is why this assertion is on the host and not only on the path.
     * </p>
     *
     * <p>
     * Only the browser-facing endpoint moves. {@code token-uri}, {@code jwk-set-uri} and {@code user-info-uri} are
     * called by this gateway rather than by a browser and stay pointed at uaa's own address.
     * </p>
     */
    @Test
    void startingALoginForwardsTheDeepLinkToUaaOnThisOriginAndOpensASession() {
        client.get().uri("/oauth2/authorization/uaa?redirectTo={to}", DEEP_LINK).exchange()
                .expectStatus().isFound()
                .expectCookie().exists("STORE-CORE-GATEWAY-JSESSIONID")
                .expectHeader().value(HttpHeaders.LOCATION, location -> {
                    var query = UriComponentsBuilder.fromUri(URI.create(location)).build().getQueryParams();
                    assertThat(location).startsWith("http://localhost:%d/uaa/oauth2/authorize".formatted(port));
                    assertThat(query.getFirst("client_id")).isEqualTo("web-app");
                    assertThat(query.getFirst("state")).isNotBlank();
                    assertThat(UriUtils.decode(query.getFirst("redirectTo"), StandardCharsets.UTF_8)).isEqualTo(DEEP_LINK);
                });
    }

    /**
     * The in-memory session count is a load-test capacity signal (Auth dashboard); starting a login opens a session,
     * and the gauge reads the store the gateway actually holds them in.
     */
    @Test
    void openSessionsAreCountedByTheSessionGauge() {
        client.get().uri("/oauth2/authorization/uaa").exchange().expectStatus().isFound();

        client.get().uri("/actuator/metrics/cvhome.gateway.sessions").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.measurements[0].value").value(v -> assertThat(((Number) v).doubleValue()).isGreaterThanOrEqualTo(1.0));
    }

    @Test
    void currentUserIsUnauthorizedWithoutALogin() {
        client.get().uri("/api/v1/auth/current").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void logoutWithoutALoginLandsOnTheLoginPage() {
        client.get().uri("/logout").exchange()
                .expectStatus().isFound()
                .expectHeader().valueEquals(HttpHeaders.LOCATION, "/login?logout");
    }

}
