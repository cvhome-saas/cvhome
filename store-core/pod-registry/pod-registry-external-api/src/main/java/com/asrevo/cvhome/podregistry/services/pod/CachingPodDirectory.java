package com.asrevo.cvhome.podregistry.services.pod;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;

import lombok.extern.slf4j.Slf4j;

/**
 * Resolves a {@link PodId} to its {@link Pod}, without putting the registry in the request path.
 *
 * <p>
 * <b>It degrades open.</b> A failed refresh keeps the last known map; if none was ever fetched, the configuration
 * seed answers. The consumer is a read that decorates a response — which pod hosts a store — and a registry outage
 * must not turn that into a 502 on a screen the seller uses to reach their own store. This is the opposite of how
 * store <em>creation</em> treats the same service being down, and the asymmetry is deliberate: placing a store on
 * an unconfirmed pod is unrecoverable, while showing a slightly stale endpoint is not.
 * </p>
 *
 * <p>
 * It caches the whole list rather than single pods. There are a handful of pods, they change when infrastructure
 * changes, and one fetch answers every lookup — a per-id cache would multiply calls for no benefit and would have
 * to consult the super-admin-only by-id endpoint.
 * </p>
 *
 * <p>
 * Lives in the client SDK, like billing's {@code StoreEntitlements}, so the caching and the degrade rule are
 * decided once rather than per caller. It takes its seed as a parameter rather than reading configuration itself,
 * which keeps this module free of any dependency on {@code store-commons:autoconfigure}.
 * </p>
 */
@Slf4j
public class CachingPodDirectory {

    private final ExternalPodService podService;

    private final Duration ttl;

    private final AtomicReference<Map<PodId, Pod>> known;

    private final AtomicReference<Instant> fetchedAt = new AtomicReference<>();

    /**
     * @param seed pods known from configuration, so a caller that starts while the registry is down still resolves
     *             the pods it was deployed alongside. A mitigation, not a cure: a pod created since then is missing
     *             until the first successful fetch
     */
    public CachingPodDirectory(ExternalPodService podService, List<Pod> seed, Duration ttl) {
        this.podService = podService;
        this.ttl = ttl;
        this.known = new AtomicReference<>(index(seed));
        log.info("Pod directory seeded with {} pod(s) from configuration", known.get().size());
    }

    public Optional<Pod> find(PodId podId) {
        if (Objects.isNull(podId)) {
            return Optional.empty();
        }
        Map<PodId, Pod> current = refreshIfStale();
        Pod pod = current.get(podId);
        if (Objects.isNull(pod)) {
            // Either genuinely unknown, or known only to a registry we could not reach. The caller gets an empty
            // Optional either way and decides; this is not the place to invent a 404.
            log.debug("Pod {} is not in the directory ({} known)", podId, current.size());
        }
        return Optional.ofNullable(pod);
    }

    private Map<PodId, Pod> refreshIfStale() {
        Instant last = fetchedAt.get();
        if (Objects.nonNull(last) && Duration.between(last, Instant.now()).compareTo(ttl) < 0) {
            return known.get();
        }
        try {
            Map<PodId, Pod> fresh = index(podService.listPods());
            known.set(fresh);
            fetchedAt.set(Instant.now());
            return fresh;
        } catch (Exception e) {
            // Deliberately the base type and deliberately only a warning: every failure mode has the same answer,
            // which is to keep serving what we already know. Re-raising would push a registry outage onto a screen
            // that only needs a pod's endpoint.
            log.warn("Could not refresh the pod directory; keeping {} known pod(s)", known.get().size(), e);
            return known.get();
        }
    }

    private static Map<PodId, Pod> index(List<Pod> pods) {
        if (Objects.isNull(pods)) {
            return Map.of();
        }
        return pods.stream()
                .filter(it -> Objects.nonNull(it.id()))
                .collect(Collectors.toMap(Pod::id, Function.identity(), (a, b) -> a));
    }

}
