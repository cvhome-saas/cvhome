package com.asrevo.cvhome.gateway.config;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import com.asrevo.cvhome.gateway.client.StoreBillingStatusClient;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Only seller writes to a lapsed store are refused, with 402 and a problem body; reads, non-pod paths and stores in
 * good standing pass untouched.
 */
class StoreBillingGuardFilterTest {

    private static final String BLOCKED = "65f023632bc46470c104b76f";

    private static final String ACTIVE = "65f023632bc46470c104b75f";

    private static final String POD_URL = "http://localhost/spg/api/v1/products?store=%s&pod=x";

    private final StoreBillingStatusClient billing = mock(StoreBillingStatusClient.class);

    private final GatewayFilterChain chain = mock(GatewayFilterChain.class);

    private final StoreBillingGuardFilter filter = new StoreBillingGuardFilter(billing);

    private MockServerWebExchange exchange(HttpMethod method, String url) {
        when(chain.filter(any())).thenReturn(Mono.empty());
        when(billing.blocked(BLOCKED)).thenReturn(true);
        return MockServerWebExchange.from(MockServerHttpRequest.method(method, URI.create(url)).build());
    }

    @Test
    void writeToABlockedStoreIsRefusedWithPaymentRequired() {
        MockServerWebExchange exchange = exchange(HttpMethod.POST, POD_URL.formatted(BLOCKED));

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.PAYMENT_REQUIRED);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(exchange.getResponse().getBodyAsString().block()).contains("BILLING.STORE.SUSPENDED");
        verify(chain, never()).filter(any());
    }

    @Test
    void readOfABlockedStoreStillPasses() {
        MockServerWebExchange exchange = exchange(HttpMethod.GET, POD_URL.formatted(BLOCKED));

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(chain).filter(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void writeToAStoreInGoodStandingPasses() {
        MockServerWebExchange exchange = exchange(HttpMethod.DELETE, POD_URL.formatted(ACTIVE));

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void nonPodPathsAreNeverConsulted() {
        MockServerWebExchange exchange = exchange(HttpMethod.POST,
                "http://localhost/tenancy/api/v1/stores?store=%s".formatted(BLOCKED));

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(chain).filter(exchange);
        verify(billing, never()).blocked(any());
    }

    @Test
    void runsAheadOfRoutingButAfterTheHighestPrecedence() {
        assertThat(filter.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE + 100);
    }

}
