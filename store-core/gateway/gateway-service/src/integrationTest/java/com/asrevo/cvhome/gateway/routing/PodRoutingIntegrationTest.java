package com.asrevo.cvhome.gateway.routing;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.asrevo.cvhome.billing.services.entitlement.ReactiveExternalEntitlementService;
import com.asrevo.cvhome.commons.domain.EndpointType;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodEndpoint;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.gateway.client.PodClient;
import com.asrevo.cvhome.gateway.client.StoreBillingStatusClient;
import com.asrevo.cvhome.podregistry.api.ReactiveExternalPodService;
import com.asrevo.cvhome.testsupport.annotations.ReactiveIntegrationTest;
import com.asrevo.cvhome.testsupport.security.Tokens;

import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Seller traffic end to end: {@code /spg/**?store=&pod=} is routed to the pod the registry reported, with the prefix
 * stripped, and the billing guard turns away writes for a suspended store before they reach the pod.
 */
@ReactiveIntegrationTest
class PodRoutingIntegrationTest {

    private static final String POD_ID = "507f1f77bcf86cd799439011";

    private static final String OTHER_POD_ID = "607f1f77bcf86cd799439012";

    private static final String LOCAL = "http://localhost:%d";

    private static final String METHOD = "$.method";

    private static final String PRODUCTS = "/spg/echo/api/v1/products?store={store}&pod={pod}";

    @LocalServerPort
    private int port;

    @Autowired
    private PodClient podClient;

    @Autowired
    private StoreBillingStatusClient billingStatusClient;

    @MockitoBean
    private ReactiveExternalPodService podService;

    @MockitoBean
    private ReactiveExternalEntitlementService entitlementService;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer().baseUrl(LOCAL.formatted(port)).build();
        Pod local = new Pod(new PodId(POD_ID), "pod-local",
                new PodEndpoint(LOCAL.formatted(port), EndpointType.EXTERNAL),
                new ManagerOrgId(Tokens.ORG_1), null);
        Mockito.when(podService.listPods()).thenReturn(Mono.just(List.of(local)));
        Mockito.when(entitlementService.blockedStores())
                .thenReturn(Mono.just(List.of(new StoreMerchantId(Tokens.STORE_2))));
        podClient.refreshRoutes();
        billingStatusClient.refresh();
    }

    @Test
    void storeRequestReachesItsPodWithThePrefixStripped() {
        client.get().uri(PRODUCTS, Tokens.STORE_1, POD_ID).exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .value(body -> {
                    assertThat(body).containsEntry("path", "/echo/api/v1/products");
                    assertThat(body.get("query")).asString().contains("store=%s".formatted(Tokens.STORE_1));
                });
    }

    @Test
    void unknownPodMatchesNoRoute() {
        client.get().uri(PRODUCTS, Tokens.STORE_1, OTHER_POD_ID).exchange().expectStatus().isNotFound();
    }

    @Test
    void requestWithoutAStoreMatchesNoRoute() {
        client.get().uri("/spg/echo/api/v1/products?pod={pod}", POD_ID).exchange().expectStatus().isNotFound();
    }

    @Test
    void writeForASuspendedStoreIsRefusedBeforeReachingThePod() {
        client.post().uri(PRODUCTS, Tokens.STORE_2, POD_ID).exchange()
                .expectStatus().isEqualTo(HttpStatus.PAYMENT_REQUIRED)
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody().jsonPath("$.code").isEqualTo("BILLING.STORE.SUSPENDED");
    }

    @Test
    void readForASuspendedStoreStillReachesThePod() {
        client.get().uri(PRODUCTS, Tokens.STORE_2, POD_ID).exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath(METHOD).isEqualTo("GET");
    }

    @Test
    void writeForAStoreInGoodStandingReachesThePod() {
        client.post().uri(PRODUCTS, Tokens.STORE_1, POD_ID).exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath(METHOD).isEqualTo("POST");
    }

    @Test
    void podRoutesReportHealthyOnceRefreshed() {
        client.get().uri("/actuator/health").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.components.podRoutes.status").isEqualTo("UP")
                .jsonPath("$.components.podRoutes.details.routes").isEqualTo(1);
    }

}
