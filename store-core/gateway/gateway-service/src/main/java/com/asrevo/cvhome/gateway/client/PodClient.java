package com.asrevo.cvhome.gateway.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.controlplane.pod.api.ExternalPodClient;
import com.asrevo.cvhome.s2s.config.internal.ServiceUrlBuilder;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class PodClient implements RouteDefinitionRepository {

    private static final List<FilterDefinition> commonFilters = List.of(new FilterDefinition("StripPrefix=1"),
            new FilterDefinition("TokenRelay"));

    private static final List<PredicateDefinition> commonPredicates = List
            .of(new PredicateDefinition("Path=/spg/**"), new PredicateDefinition("Query=store"));

    private final ServiceDomainProperties serviceDomainProperties;

    private final ExternalPodClient externalPodClient;

    private final Environment environment;

    private final ApplicationEventPublisher publisher;

    private final ObservationRegistry observationRegistry;

    @Scheduled(fixedRateString = "${cvhome.gateway.route-refresh-rate:PT1M}")
    public void refreshRoutes() {
        Observation.createNotStarted("cvhome.gateway.refresh-routes", observationRegistry)
                .observe(() -> publisher.publishEvent(new RefreshRoutesEvent(this)));
    }

    public Flux<Pod> getPods() {
        return externalPodClient.listPods().onErrorResume(e -> {
            log.error("Could not fetch pods from control-plane", e);
            return Mono.empty();
        }).flatMapMany(Flux::fromIterable);
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        ServiceUrlBuilder serviceUrlBuilder = new ServiceUrlBuilder(serviceDomainProperties, environment);
        return getPods().map(pod -> {
            RouteDefinition rd = new RouteDefinition();
            rd.setId("pod-" + pod.shortenPodId());
            rd.setUri(URI.create(serviceUrlBuilder.getServiceUrl(pod)));

            var predicates = new ArrayList<>(commonPredicates);
            predicates.add(new PredicateDefinition("Query=pod," + pod.id().id()));
            rd.setPredicates(predicates);

            rd.setFilters(commonFilters);
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
