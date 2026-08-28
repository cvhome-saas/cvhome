package com.asrevo.cvhome.gateway.routing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.asrevo.cvhome.billing.services.entitlement.ReactiveExternalEntitlementService;
import com.asrevo.cvhome.podregistry.api.ReactiveExternalPodService;
import com.asrevo.cvhome.testsupport.annotations.ReactiveIntegrationTest;

/**
 * The static routes over real HTTP. No backend instance is registered with the load balancer, so a matched route
 * answers 503 (no instance) while an unmatched request answers 404 — the difference between "routed" and "dropped".
 */
@ReactiveIntegrationTest
class ConsoleRoutingIntegrationTest {

    private static final String GATEWAY_HOST = "gateway.com";

    private static final String DASHBOARD = "/dashboard";

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
    void backendPrefixesAreForwardedToTheirService() {
        for (String backend : new String[] {"tenancy", "billing", "pod-registry", "uaa"}) {
            client.get().uri("/%s/api/v1/anything".formatted(backend)).exchange()
                    .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @Test
    void consoleShellIsServedOnTheGatewayHostNames() {
        for (String host : new String[] {GATEWAY_HOST, "www.gateway.com", "console-ui.gateway.com"}) {
            client.get().uri(DASHBOARD).header(HttpHeaders.HOST, host).exchange()
                    .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @Test
    void backendPrefixOnTheGatewayHostIsNotSwallowedByTheConsole() {
        client.post().uri("/billing/api/v1/plans").header(HttpHeaders.HOST, GATEWAY_HOST).exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void unknownHostIsNotRoutedAnywhere() {
        client.get().uri(DASHBOARD).exchange().expectStatus().isNotFound();
    }

}
