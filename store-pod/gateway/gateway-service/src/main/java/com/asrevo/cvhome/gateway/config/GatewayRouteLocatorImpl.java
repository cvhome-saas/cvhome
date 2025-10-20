package com.asrevo.cvhome.gateway.config;

import com.asrevo.cvhome.s2s.config.gateway.FHostRoutePredicateFactory;
import com.asrevo.cvhome.s2s.config.gateway.FNotServiceRoutePredicateFactory;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import java.util.Set;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class GatewayRouteLocatorImpl implements RouteLocator {
    private final RouteLocatorBuilder routeLocatorBuilder;
    private final ServiceDomainProperties serviceDomainProperties;
    private final FNotServiceRoutePredicateFactory notServicePredicate;
    private final FHostRoutePredicateFactory hostRoutePredicate;

    @Override
    public Flux<Route> getRoutes() {
        String storeCoreGatewayDomain =
                serviceDomainProperties.getService("store-core-gateway").domain();
        Set<String> backendServices =
                Set.of(
                        "merchant",
                        "content",
                        "catalog",
                        "order",
                        "merchant-ui",
                        "landing-ui",
                        "pod-auth");
        Predicate<ServerWebExchange> notBackendService =
                notServicePredicate.apply(
                        new FNotServiceRoutePredicateFactory.Config(backendServices));

        Predicate<ServerWebExchange> merchantUiHostPredicate =
                hostRoutePredicate.apply(
                        config -> config.setHost(Set.of("merchant-ui." + storeCoreGatewayDomain)));
        Predicate<ServerWebExchange> podAuthHostPredicate =
                hostRoutePredicate.apply(
                        config -> config.setHost(Set.of("pod-auth." + storeCoreGatewayDomain)));

        RouteLocatorBuilder.Builder routes = routeLocatorBuilder.routes();

        backendServices.forEach(
                bs ->
                        routes.route(
                                        r ->
                                                r.path("/" + bs + "/**")
                                                        .filters(
                                                                f ->
                                                                        f.stripPrefix(1)
                                                                                .tokenRelay()
                                                                                .preserveHostHeader())
                                                        .uri("lb://" + bs))
                                .route(
                                        r ->
                                                r.path("/store-pod-gateway/" + bs + "/**")
                                                        .filters(
                                                                f ->
                                                                        f.stripPrefix(2)
                                                                                .tokenRelay()
                                                                                .preserveHostHeader())
                                                        .uri("lb://" + bs)));

        routes.route(
                        r ->
                                r.predicate(notBackendService)
                                        .and()
                                        .predicate(podAuthHostPredicate)
                                        .uri("lb://pod-auth"))
                .route(
                        r ->
                                r.predicate(notBackendService)
                                        .and()
                                        .predicate(merchantUiHostPredicate)
                                        .uri("lb://merchant-ui"))
                .route(r -> r.predicate(notBackendService).uri("lb://landing-ui"));
        return routes.build().getRoutes();
    }
}
