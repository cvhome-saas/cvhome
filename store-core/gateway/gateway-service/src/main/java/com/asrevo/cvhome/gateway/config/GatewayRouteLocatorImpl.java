package com.asrevo.cvhome.gateway.config;

import java.util.Arrays;

import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;

import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class GatewayRouteLocatorImpl implements RouteLocator {

    // Every path prefix that belongs to a backend. The array is negated below to build seller-ui's catch-all, so a
    // service missing from here is not merely unrouted — its calls are answered with the console's shell HTML.
    private static final String[] backendServices = {"control-plane", "billing", "uaa", "spg"};

    private static final String[] backendServicesPattern = Arrays.stream(backendServices)
            .map(it -> String.format("/%s/**", it))
            .toArray(String[]::new);

    private final RouteLocatorBuilder routeLocatorBuilder;

    private final ServiceDomainProperties serviceDomainProperties;

    private final Environment environment;

    @Override
    public Flux<Route> getRoutes() {
        String gatewayService = environment.getProperty("spring.application.name");
        String storeCoreGatewayDomain = serviceDomainProperties.getService(gatewayService).domain();

        return routeLocatorBuilder.routes()
                .route(r -> r.path("/control-plane/**")
                        .filters(f -> f.stripPrefix(1).tokenRelay().preserveHostHeader())
                        .uri("lb://control-plane"))
                .route(r -> r.path("/billing/**")
                        .filters(f -> f.stripPrefix(1).tokenRelay().preserveHostHeader())
                        .uri("lb://billing"))
                .route(r -> r.path(backendServicesPattern)
                        .negate()
                        .and()
                        .host(storeCoreGatewayDomain, String.format("www.%s", storeCoreGatewayDomain),
                                String.format("seller-ui.%s", storeCoreGatewayDomain))
                        .uri("lb://seller-ui"))
                .build()
                .getRoutes();
    }

}
