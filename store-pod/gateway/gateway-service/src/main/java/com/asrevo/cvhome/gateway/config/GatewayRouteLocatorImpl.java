package com.asrevo.cvhome.gateway.config;

import com.asrevo.cvhome.gateway.filters.AddStoreParamGatewayFilterFactory;
import com.asrevo.cvhome.s2s.config.gateway.FHostRoutePredicateFactory;
import com.asrevo.cvhome.s2s.config.gateway.FNotServiceRoutePredicateFactory;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

import java.util.Set;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class GatewayRouteLocatorImpl implements RouteLocator {
    private final RouteLocatorBuilder routeLocatorBuilder;
    private final ServiceDomainProperties serviceDomainProperties;
    private final FNotServiceRoutePredicateFactory notServicePredicate;
    private final FHostRoutePredicateFactory hostRoutePredicate;
    private final AddStoreParamGatewayFilterFactory addStoreParamGatewayFilterFactory;

    @Override
    public Flux<Route> getRoutes() {
        String storeCoreGatewayDomain = serviceDomainProperties.getService("store-core-gateway").domain();
        Set<String> backendServices = Set.of("merchant", "content", "catalog", "order", "merchant-ui", "landing-ui");
        Predicate<ServerWebExchange> notBackendService = notServicePredicate.apply(new FNotServiceRoutePredicateFactory.Config(backendServices));

        Predicate<ServerWebExchange> merchantUiHostPredicate = hostRoutePredicate.apply(config -> config.setHost(Set.of("merchant-ui." + storeCoreGatewayDomain)));

        GatewayFilter addStoreParamGatewayFilter = addStoreParamGatewayFilterFactory.apply(config -> {
            config.setAddRequestHeader(true);
            config.setAddResponseHeader(true);
        });

        RouteLocatorBuilder.Builder routes = routeLocatorBuilder.routes();

        backendServices.forEach(bs -> routes
                .route(r -> r
                        .path("/" + bs + "/**")
                        .filters(f -> f.stripPrefix(1).tokenRelay().preserveHostHeader())
                        .uri("lb://" + bs))
                .route(r -> r
                        .path("/store-pod-gateway/" + bs + "/**")
                        .filters(f -> f.stripPrefix(2).tokenRelay().preserveHostHeader())
                        .uri("lb://" + bs))
        );

        routes
                .route(r -> r
                        .predicate(notBackendService)
                        .and()
                        .predicate(merchantUiHostPredicate)
                        .uri("lb://merchant-ui"))
                .route(r -> r
                        .predicate(notBackendService)
                        .filters(spec -> spec.filter(addStoreParamGatewayFilter))
                        .uri("lb://landing-ui"));
        return routes.build().getRoutes();


    }
}
