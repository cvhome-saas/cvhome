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

    /**
     * The path uaa answers on through this gateway, and the value it is told to call its own.
     *
     * <p>
     * Public because the authorization request resolver builds a browser-facing authorize URL on this same
     * gateway and has to agree with the route about where uaa is; a disagreement here is a 404 in the middle of
     * a sign-in.
     * </p>
     */
    public static final String UAA_PREFIX = "/uaa";

    // Every path prefix that belongs to a backend. The array is negated below to build console-ui's catch-all, so a
    // service missing from here is not merely unrouted — its calls are answered with the console's shell HTML.
    private static final String[] backendServices = {"tenancy", "billing", "pod-registry", "uaa", "spg"};

    private static final String FORWARDED_PREFIX = "X-Forwarded-Prefix";

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
                .route(r -> r.path(String.format("%s/**", UAA_PREFIX))
                        /*
                         * The one route that does not strip its prefix, and the only one that must not.
                         *
                         * uaa builds absolute URLs now — it redirects a browser to the console's sign-in page and
                         * has to say which origin to come back to — so it reads X-Forwarded-Prefix and reports it
                         * as its context path. Spring requires the context path to be the literal start of the
                         * request path, so stripping the prefix and then naming it in a header is a contradiction
                         * it rejects outright:
                         *
                         *   IllegalArgumentException: Invalid contextPath '/uaa': must match the start of
                         *   requestPath: '/oauth2/authorize'
                         *
                         * — which surfaces as a 500 dispatched to /error on every browser hop, not as a bad URL.
                         * Forwarding the path intact is what spg already does for cua (`handle /cua*`, never
                         * `handle_path`); uaa's PathPrefixFilter then takes the prefix back off for routing, so
                         * the endpoints underneath are addressed exactly as before.
                         */
                        .filters(f -> f.tokenRelay()
                                .preserveHostHeader()
                                .addRequestHeader(FORWARDED_PREFIX, UAA_PREFIX))
                        .uri("lb://uaa"))
                /*
                 * preserveHostHeader, like every route above. Without it the Host forwarded to console-ui is the
                 * address discovery resolved — localhost:8011 locally, but the task's private IP on Fargate, where
                 * lb:// goes through Cloud Map. The console renders server-side, so that Host is what its SSR pass
                 * sees as the request's own origin; leaving it as 10.0.0.97:8011 puts a private address where the
                 * public one belongs. This route was the only one that had never asked for the real host.
                 */
                .route(r -> r.path(backendServicesPattern)
                        .negate()
                        .and()
                        .host(storeCoreGatewayDomain, String.format("www.%s", storeCoreGatewayDomain),
                                String.format("console-ui.%s", storeCoreGatewayDomain))
                        .filters(f -> f.preserveHostHeader())
                        .uri("lb://console-ui"))
                .build()
                .getRoutes();
    }

}
