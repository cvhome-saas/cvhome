package com.asrevo.cvhome.gateway.client;

/**
 * Import statements:
 * com.asrevo.cvhome.commons.domain.Pod
 * com.asrevo.cvhome.s2s.config.internal.ServiceUrlBuilder
 * com.asrevo.cvhome.s2s.model.ServiceDomainProperties
 * lombok.RequiredArgsConstructor
 * org.springframework.cloud.gateway.filter.FilterDefinition
 * org.springframework.cloud.gateway.handler.predicate.PredicateDefinition
 * org.springframework.cloud.gateway.route.RouteDefinition
 * org.springframework.cloud.gateway.route.RouteDefinitionRepository
 * org.springframework.core.env.Environment
 * org.springframework.stereotype.Component
 * reactor.core.publisher.Flux
 * reactor.core.publisher.Mono
 * java.net.URI
 * java.util.List
 * java.util.Optional
 */
import com.asrevo.cvhome.commons.domain.Pod;
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
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PodClient implements RouteDefinitionRepository {

	private final ServiceDomainProperties serviceDomainProperties;

	private final Environment environment;

	public Flux<Pod> getPods() {
		return Flux.fromIterable(Optional.ofNullable(serviceDomainProperties.pods()).orElse(List.of()));
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
