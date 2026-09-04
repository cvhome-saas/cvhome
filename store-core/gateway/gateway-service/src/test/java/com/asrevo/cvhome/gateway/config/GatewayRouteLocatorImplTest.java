package com.asrevo.cvhome.gateway.config;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webflux.autoconfigure.WebFluxProperties;
import org.springframework.cloud.gateway.filter.factory.AddRequestHeaderGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.PreserveHostHeaderGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.StripPrefixGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.TokenRelayGatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.HostRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import com.asrevo.cvhome.commons.domain.ServiceDomain;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;

import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The static route table: each backend prefix goes to its service, and everything else on the gateway's own host names
 * is the console's shell — so a backend prefix must never fall into the console catch-all.
 */
class GatewayRouteLocatorImplTest {

    private static final String GATEWAY = "store-core-gateway";

    private static final String DOMAIN = "gateway.com";

    private static final String LOCALHOST = "localhost";

    private static final String DASHBOARD = "/dashboard";

    private static final String STRIP_PREFIX = "StripPrefix";

    private GenericApplicationContext context;

    private List<Route> routes;

    private static ServerWebExchange request(String host, String path) {
        return MockServerWebExchange
                .from(MockServerHttpRequest.get(URI.create("http://%s%s".formatted(host, path)).toString())
                        .header(HttpHeaders.HOST, host)
                        .build());
    }

    private Route matching(ServerWebExchange exchange) {
        return routes.stream()
                .filter(route -> Boolean.TRUE.equals(Mono.from(route.getPredicate().apply(exchange)).block()))
                .findFirst()
                .orElse(null);
    }

    @BeforeEach
    void setUp() {
        context = new GenericApplicationContext();
        context.registerBean(WebFluxProperties.class);
        context.registerBean(PathRoutePredicateFactory.class);
        context.registerBean(HostRoutePredicateFactory.class);
        context.registerBean(StripPrefixGatewayFilterFactory.class);
        context.registerBean(AddRequestHeaderGatewayFilterFactory.class);
        context.registerBean(PreserveHostHeaderGatewayFilterFactory.class);
        context.registerBean(TokenRelayGatewayFilterFactory.class,
                () -> new TokenRelayGatewayFilterFactory(context.getBeanProvider(
                        org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager.class)));
        context.refresh();

        ServiceDomainProperties properties = new ServiceDomainProperties(
                Map.of(GATEWAY, new ServiceDomain(GATEWAY, DOMAIN, "8000", "http", "store-core.cvhome.lcl", GATEWAY)),
                List.of());
        MockEnvironment environment = new MockEnvironment().withProperty("spring.application.name", GATEWAY);
        routes = new GatewayRouteLocatorImpl(new RouteLocatorBuilder(context), properties, environment).getRoutes()
                .collectList()
                .block();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    private static String filtersOf(Route route) {
        return route.getFilters().toString();
    }

    @Test
    void everyBackendPrefixHasARouteWithRelayAndStrip() {
        assertThat(routes).hasSize(5);
        for (String backend : List.of("tenancy", "billing", "pod-registry")) {
            Route route = matching(request(LOCALHOST, "/%s/api/v1/x".formatted(backend)));
            assertThat(route).as(backend).isNotNull();
            assertThat(route.getUri()).hasToString("lb://%s".formatted(backend));
            assertThat(route.getFilters()).hasSize(3);
            assertThat(filtersOf(route)).as(backend).contains(STRIP_PREFIX);
        }
    }

    /**
     * uaa is the exception, and it has to stay one.
     *
     * <p>
     * uaa builds absolute URLs — it redirects a browser to the console's sign-in page — so it reads
     * {@code X-Forwarded-Prefix} and reports it as its context path. Spring requires the context path to be the
     * literal start of the request path, so a route that strips the prefix and then names it in the header is a
     * contradiction Spring rejects: {@code Invalid contextPath '/uaa': must match the start of requestPath}. That
     * arrives as a 500 dispatched to {@code /error} on every browser hop of a sign-in, which is a slow thing to
     * diagnose and an easy thing to reintroduce by making this route look like its neighbours.
     * </p>
     */
    @Test
    void theUaaRouteForwardsItsPrefixInsteadOfStrippingIt() {
        Route route = matching(request(LOCALHOST, "/uaa/oauth2/authorize"));

        assertThat(route).isNotNull();
        assertThat(route.getUri()).hasToString("lb://uaa");
        assertThat(filtersOf(route)).doesNotContain(STRIP_PREFIX).contains("X-Forwarded-Prefix");
    }

    /**
     * The filter is preserveHostHeader. console-ui's SSR server validates the Host header against the allowlist baked
     * into its build, so a rewritten Host — the downstream instance address, which on Fargate is a private IP — is
     * answered with a 400 rather than the console.
     */
    @Test
    void consoleCatchAllServesTheGatewayHostNamesWithTheHostPreserved() {
        for (String host : List.of(DOMAIN, "www.gateway.com", "console-ui.gateway.com")) {
            Route route = matching(request(host, DASHBOARD));
            assertThat(route).as(host).isNotNull();
            assertThat(route.getUri()).hasToString("lb://console-ui");
            assertThat(route.getFilters()).as(host).hasSize(1);
        }
    }

    @Test
    void backendPrefixOnTheGatewayHostIsNotSwallowedByTheConsole() {
        Route route = matching(request(DOMAIN, "/billing/api/v1/plans"));

        assertThat(route.getUri()).hasToString("lb://billing");
    }

    @Test
    void podTrafficAndForeignHostsMatchNoStaticRoute() {
        assertThat(matching(request(DOMAIN, "/spg/api/v1/products"))).isNull();
        assertThat(matching(request(LOCALHOST, DASHBOARD))).isNull();
    }

}
