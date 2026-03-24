package com.asrevo.cvhome.gateway.client;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.controlplane.pod.api.PodExternalClient;
import com.asrevo.cvhome.s2s.config.internal.ServiceUrlBuilder;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PodClient implements RouteDefinitionRepository {

	private final ServiceDomainProperties serviceDomainProperties;

	private final PodExternalClient podExternalClient;

	private final Environment environment;

	public Flux<Pod> getPods() {
		return podExternalClient.listPods().flatMapMany(Flux::fromIterable);
	}

	@Override
	public Flux<RouteDefinition> getRouteDefinitions() {
		ServiceUrlBuilder serviceUrlBuilder = new ServiceUrlBuilder(serviceDomainProperties, environment);
		return getPods().map(pod -> {
			RouteDefinition rd = new RouteDefinition();
			rd.setId("pod-" + pod.id().id());
			rd.setUri(URI.create(serviceUrlBuilder.getServiceUrl(pod)));

			rd.setPredicates(List.of(new PredicateDefinition("Path=/store-pod-gateway/**"),
					new PredicateDefinition("Query=store"), new PredicateDefinition("Query=pod," + pod.id().id())));

			rd.setFilters(List.of(new FilterDefinition("StripPrefix=1"), new FilterDefinition("TokenRelay")));
			return rd;
		});
	}

	@Override
	public Mono<Void> save(Mono<RouteDefinition> route) {
		return Mono.empty();
	}

	@Override
	public Mono<Void> delete(Mono<String> routeId) {
		return Mono.empty();
	}

}
