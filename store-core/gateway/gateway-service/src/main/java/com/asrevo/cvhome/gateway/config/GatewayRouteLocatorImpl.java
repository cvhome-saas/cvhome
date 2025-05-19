package com.asrevo.cvhome.gateway.config;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.manager.api.CachedRouterService;
import com.asrevo.cvhome.s2s.config.gateway.FHostRoutePredicateFactory;
import com.asrevo.cvhome.s2s.config.gateway.FNotServiceRoutePredicateFactory;
import com.asrevo.cvhome.s2s.config.internal.ServiceUrlBuilder;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.handler.AsyncPredicate;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class GatewayRouteLocatorImpl implements RouteLocator {
    private final RouteLocatorBuilder routeLocatorBuilder;
    private final ServiceDomainProperties serviceDomainProperties;
    private final FNotServiceRoutePredicateFactory notServicePredicate;
    private final FHostRoutePredicateFactory hostRoutePredicate;
    private final CachedRouterService router;
    private final Environment environment;

    @Override
    public Flux<Route> getRoutes() {
        String storeCoreGatewayDomain = serviceDomainProperties.getService("store-core-gateway").domain();
        Set<String> backendServices = Set.of("manager", "subscription", "auth", "store-pod-gateway", "store-pod-gateway-v2", "merchant-ui");
        Predicate<ServerWebExchange> notBackendService = notServicePredicate.apply(new FNotServiceRoutePredicateFactory.Config(backendServices));
        Predicate<ServerWebExchange> storeUiHostPredicate = hostRoutePredicate.apply(new FHostRoutePredicateFactory.Config(Set.of("store-ui." + storeCoreGatewayDomain)));
        Predicate<ServerWebExchange> wwwHostPredicate = hostRoutePredicate.apply(new FHostRoutePredicateFactory.Config(Set.of(storeCoreGatewayDomain, "www." + storeCoreGatewayDomain)));

        RouteLocatorBuilder.Builder route = routeLocatorBuilder.routes()
                .route(r -> r
                        .path("/manager/**")
                        .filters(f -> f.stripPrefix(1).tokenRelay().preserveHostHeader())
                        .uri("lb://manager")
                )
                .route(r -> r
                        .path("/subscription/**")
                        .filters(f -> f.stripPrefix(1).tokenRelay().preserveHostHeader())
                        .uri("lb://subscription")
                )
                .route(r -> {
                            return r
                                    .predicate(notBackendService)
                                    .and()
                                    .predicate(storeUiHostPredicate)
                                    .uri("lb://store-ui");
                        }
                )
                .route(r -> {
                            return r
                                    .predicate(notBackendService)
                                    .and()
                                    .predicate(wwwHostPredicate)
                                    .uri("lb://welcome-ui");
                        }
                );

        List<Pod> allPods = Optional.ofNullable(serviceDomainProperties.pods()).orElse(List.of());
        for (Pod podNameSpace : allPods) {
            addPodServiceExactMatchPod(podNameSpace, route, "store-pod-gateway");
        }


        return route.build()
                .getRoutes();

    }

    private void addPodServiceExactMatchPod(Pod pod, RouteLocatorBuilder.Builder route, String serviceName) {
        ServiceUrlBuilder serviceUrlBuilder = new ServiceUrlBuilder(serviceDomainProperties, environment);
        route.route("store-" + serviceName + "-v1-" + pod.id().id(),
                r -> {
                    return r.path("/" + serviceName + "/**")
                            .and().asyncPredicate(checkStoreMatchPodFromParam(pod))
                            .filters(f -> f.stripPrefix(1).tokenRelay().preserveHostHeader())
                            .uri(serviceUrlBuilder.getServiceUrl(pod));
                });
        route.route("store-" + serviceName + "-v2-" + pod.id().id(),
                r -> {
                    return r.path("/" + serviceName + "-v2/**")
                            .and().asyncPredicate(checkStoreMatchPodFromUrl(pod))
                            .filters(f -> f.stripPrefix(2).tokenRelay().preserveHostHeader())
                            .uri(serviceUrlBuilder.getServiceUrl(pod));
                });
    }

    private AsyncPredicate<ServerWebExchange> checkStoreMatchPodFromParam(Pod pod) {
        return serverWebExchange -> {
            ManagerStoreId store = Optional.ofNullable(serverWebExchange.getRequest().getQueryParams().getFirst("store"))
                    .filter(it -> !it.trim().isEmpty())
                    .map(ManagerStoreId::new)
                    .orElse(null);
            return isStoreInPod(store, pod);
        };
    }

    private AsyncPredicate<ServerWebExchange> checkStoreMatchPodFromUrl(Pod pod) {
        return serverWebExchange -> {
            ManagerStoreId store = Optional.ofNullable(serverWebExchange.getRequest().getURI().getPath())
                    .map(url -> new ManagerStoreId(url.split("/")[2]))
                    .orElse(null);
            return isStoreInPod(store, pod);
        };
    }

    private Mono<Boolean> isStoreInPod(ManagerStoreId store, Pod pod) {
        if (store == null || pod == null) {
            return Mono.just(Boolean.FALSE);
        }
        return this.router.getStorePodByStoreId(store)
                .map(it -> it.id().equals(pod.id()))
                .defaultIfEmpty(Boolean.FALSE);
    }
}
