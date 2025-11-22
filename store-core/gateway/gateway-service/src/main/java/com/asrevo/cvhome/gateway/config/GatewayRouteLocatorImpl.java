package com.asrevo.cvhome.gateway.config;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
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

	private final Environment environment;

	@Override
	public Flux<Route> getRoutes() {
		String storeCoreGatewayDomain = serviceDomainProperties.getService("store-core-gateway").domain();
		Set<String> backendServices = Set.of("control-plane", "core-auth", "store-pod-gateway", "store-pod-gateway-v2");
		Predicate<ServerWebExchange> notBackendService = notServicePredicate
			.apply(new FNotServiceRoutePredicateFactory.Config(backendServices));
		Predicate<ServerWebExchange> wwwHostPredicate = hostRoutePredicate
			.apply(new FHostRoutePredicateFactory.Config(Set.of(storeCoreGatewayDomain, "www." + storeCoreGatewayDomain,
					"seller-ui." + storeCoreGatewayDomain)));

		RouteLocatorBuilder.Builder route = routeLocatorBuilder.routes()
			.route(r -> r.path("/control-plane/**")
				.filters(f -> f.stripPrefix(1).tokenRelay().preserveHostHeader())
				.uri("lb://control-plane"))
			.route(r -> r.predicate(notBackendService).and().predicate(wwwHostPredicate).uri("lb://seller-ui"));

		List<Pod> allPods = Optional.ofNullable(serviceDomainProperties.pods()).orElse(List.of());
		for (Pod podNameSpace : allPods) {
			addPodServiceExactMatchPod(podNameSpace, route);
		}

		return route.build().getRoutes();
	}

	private void addPodServiceExactMatchPod(Pod pod, RouteLocatorBuilder.Builder route) {
		ServiceUrlBuilder serviceUrlBuilder = new ServiceUrlBuilder(serviceDomainProperties, environment);
		route.route("store-store-pod-gateway-v1-" + pod.id().id(),
				r -> r.path("/store-pod-gateway/**")
					.and()
					.query("store")
					.and()
					.query("pod")
					.and()
					.asyncPredicate(checkStoreMatchPodFromParam(pod.id()))
					.filters(f -> f.stripPrefix(1).tokenRelay())
					.uri(serviceUrlBuilder.getServiceUrl(pod)));
	}

	private AsyncPredicate<ServerWebExchange> checkStoreMatchPodFromParam(PodId podId) {
		return serverWebExchange -> Optional.ofNullable(serverWebExchange.getRequest().getQueryParams().getFirst("pod"))
			.filter(it -> !it.trim().isEmpty())
			.map(PodId::new)
			.map(it -> it.equals(podId))
			.map(Mono::just)
			.orElseGet(() -> Mono.just(false));
	}

}
