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

    @Test
    void startingALoginForwardsTheDeepLinkToUaaAndOpensASession() {
        client.get().uri("/oauth2/authorization/uaa?redirectTo={to}", DEEP_LINK).exchange()
                .expectStatus().isFound()
                .expectCookie().exists("STORE-CORE-GATEWAY-JSESSIONID")
                .expectHeader().value(HttpHeaders.LOCATION, location -> {
                    var query = UriComponentsBuilder.fromUri(URI.create(location)).build().getQueryParams();
                    assertThat(location).startsWith("http://uaa.gateway.com:8001/oauth2/authorize");
                    assertThat(query.getFirst("client_id")).isEqualTo("web-app");
                    assertThat(query.getFirst("state")).isNotBlank();
                    assertThat(UriUtils.decode(query.getFirst("redirectTo"), StandardCharsets.UTF_8)).isEqualTo(DEEP_LINK);
                });
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
