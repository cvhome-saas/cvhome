package com.asrevo.cvhome.gateway.client;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.annotation.PostConstruct;

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
import com.asrevo.cvhome.podregistry.api.ReactiveExternalPodService;
import com.asrevo.cvhome.s2s.config.internal.ServiceUrlBuilder;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Builds one gateway route per pod, so {@code /spg/**?store=&pod=} reaches the pod that hosts the store.
 *
 * <p>
 * The route table is <em>fetched on a schedule and published</em>, never fetched during lookup. That ordering is the
 * whole point of this class: {@link #getRouteDefinitions()} is called by {@code CachingRouteLocator} while it rebuilds
 * its table, so doing I/O there means a slow or dead registry produces an <em>empty</em> table — and an empty table is
 * every tenant storefront returning 404 within one refresh period. Here a failed refresh leaves
 * {@link #lastKnownGood} untouched and logs; the previous routes keep serving.
 *
 * <p>
 * Stale routes are strictly better than no routes for this data: a pod's endpoint changes approximately never, and the
 * cost of being briefly stale is one misrouted pod, while the cost of being empty is total tenant downtime.
 */
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

    private final ReactiveExternalPodService podService;

    private final Environment environment;

    private final ApplicationEventPublisher publisher;

    private final AtomicReference<List<RouteDefinition>> lastKnownGood = new AtomicReference<>(List.of());

    private final AtomicReference<Instant> lastSuccessfulRefresh = new AtomicReference<>();

    /**
     * Seeds the table from configuration so a gateway that starts while the registry is down still routes the pods we
     * already know about. It is a mitigation, not a cure — a pod created since the config was written is missing until
     * the first successful refresh.
     */
    @PostConstruct
    void seedFromConfiguration() {
        List<Pod> configured = serviceDomainProperties.pods();
        if (configured == null || configured.isEmpty()) {
            log.warn("No pods configured; pod routes are empty until the first successful refresh from the registry");
            return;
        }
        lastKnownGood.set(toRouteDefinitions(configured));
        log.info("Seeded {} pod route(s) from configuration", configured.size());
    }

    /**
     * Reactive on purpose: Spring subscribes to the returned {@code Mono} itself, outside the scheduled-task
     * observation scope. Subscribing by hand from inside that scope had Reactor's context propagation restore an
     * observation the scope no longer owned, and the gateway logged "Observation … is not the same as the one set as
     * this scope's parent" on every refresh.
     */
    @Scheduled(fixedRateString = "${cvhome.gateway.route-refresh-rate:PT1M}")
    public Mono<Void> refreshRoutes() {
        // Deferred: Spring calls a reactive @Scheduled method once at startup to obtain its publisher, before any
        // test has stubbed the registry, and a null there fails the whole context. The registry is read on subscribe.
        return Mono.defer(podService::listPods)
                .map(this::toRouteDefinitions)
                .doOnNext(this::applyRefresh)
                .onErrorResume(e -> {
                    log.error("Pod route refresh failed; keeping {} known route(s)", lastKnownGood.get().size(), e);
                    return Mono.empty();
                })
                .then();
    }

    private void applyRefresh(List<RouteDefinition> fresh) {
        lastSuccessfulRefresh.set(Instant.now());
        // Only a real change is worth a RefreshRoutesEvent: publishing unconditionally made CachingRouteLocator discard
        // and rebuild its whole table every minute for nothing.
        if (!fresh.equals(lastKnownGood.getAndSet(fresh))) {
            log.info("Pod routes changed; publishing refresh with {} route(s)", fresh.size());
            publisher.publishEvent(new RefreshRoutesEvent(this));
        }
    }

    private List<RouteDefinition> toRouteDefinitions(List<Pod> pods) {
        ServiceUrlBuilder serviceUrlBuilder = new ServiceUrlBuilder(serviceDomainProperties, environment);
        List<RouteDefinition> definitions = new ArrayList<>(pods.size());
        for (Pod pod : pods) {
            RouteDefinition rd = new RouteDefinition();
            rd.setId(String.format("pod-%s", pod.shortenPodId()));
            rd.setUri(URI.create(serviceUrlBuilder.getServiceUrl(pod)));

            var predicates = new ArrayList<>(commonPredicates);
            predicates.add(new PredicateDefinition(String.format("Query=pod,%s", pod.id().id())));
            rd.setPredicates(predicates);

            rd.setFilters(commonFilters);
            definitions.add(rd);
        }
        return definitions;
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return Flux.fromIterable(lastKnownGood.get());
    }

    /**
     * How long ago the last refresh succeeded, or empty if none ever has. Read by {@code PodRoutesHealthIndicator} —
     * without it a gateway serving indefinitely stale routes looks perfectly healthy.
     */
    public Optional<Duration> timeSinceLastSuccessfulRefresh() {
        return Optional.ofNullable(lastSuccessfulRefresh.get()).map(at -> Duration.between(at, Instant.now()));
    }

    public int knownRouteCount() {
        return lastKnownGood.get().size();
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
