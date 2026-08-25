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

    // Every path prefix that belongs to a backend. The array is negated below to build console-ui's catch-all, so a
    // service missing from here is not merely unrouted — its calls are answered with the console's shell HTML.
    private static final String[] backendServices = {"tenancy", "billing", "pod-registry", "uaa", "spg"};

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
                .route(r -> r.path("/tenancy/**")
                        .filters(f -> f.stripPrefix(1).tokenRelay().preserveHostHeader())
                        .uri("lb://tenancy"))
                .route(r -> r.path("/billing/**")
                        .filters(f -> f.stripPrefix(1).tokenRelay().preserveHostHeader())
                        .uri("lb://billing"))
                .route(r -> r.path("/pod-registry/**")
                        .filters(f -> f.stripPrefix(1).tokenRelay().preserveHostHeader())
                        .uri("lb://pod-registry"))
                /*
                 * uaa's admin API, reachable from the browser tier for the first time.
                 *
                 * "uaa" was already in backendServices above — the array that is *negated* to build the UI
                 * catch-all — so /uaa/** was excluded from the console and forwarded nowhere: it matched no route
                 * at all and 404'd. The console's platform user management is the first caller that needs it.
                 *
                 * Nothing else moves. uaa's own AppSecurityConfig gates /api/v1/admin/** on SCOPE_super_admin or
                 * ROLE_SUPER_ADMIN at the filter chain and again with @PreAuthorize on every method, and it is
                 * already a JWT resource server. This relays the operator's token unchanged; uaa's guard, not the
                 * gateway's, is what keeps the admin API safe.
                 */
                .route(r -> r.path("/uaa/**")
                        .filters(f -> f.stripPrefix(1).tokenRelay().preserveHostHeader())
                        .uri("lb://uaa"))
                .route(r -> r.path(backendServicesPattern)
                        .negate()
                        .and()
                        .host(storeCoreGatewayDomain, String.format("www.%s", storeCoreGatewayDomain),
                                String.format("console-ui.%s", storeCoreGatewayDomain))
                        .uri("lb://console-ui"))
                .build()
                .getRoutes();
    }

}
