package com.asrevo.cvhome.podregistry.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.asrevo.cvhome.podregistry.commons.PodHealthStatus;
import com.asrevo.cvhome.podregistry.domain.PodEntity;
import com.asrevo.cvhome.podregistry.domain.PodHealthCheckEntity;
import com.asrevo.cvhome.podregistry.repository.PodHealthCheckRepository;
import com.asrevo.cvhome.podregistry.repository.PodRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Periodically asks each pod whether it is reachable.
 *
 * <p>
 * <strong>Health gates placement, never routing.</strong> A pod that fails this probe is excluded from taking
 * <em>new</em> stores and nothing else: it keeps its gateway route, because the stores already living there are
 * reached through it and withdrawing the route converts "degraded" into "entirely offline". That rule is enforced
 * in {@code PodPlacementService}, not here — this only records what it saw.
 * </p>
 *
 * <p>
 * It is a <em>reachability</em> probe against the pod's own endpoint, not a deep health check. Any HTTP answer —
 * including a 404 — counts as GREEN, because it proves the pod's edge is up and serving; only a connection failure
 * or a timeout is RED. That is the honest limit of what can be asked without a dedicated health endpoint behind the
 * pod gateway, and it is the signal placement actually needs: can this host take traffic at all. A deeper check
 * belongs with a real per-pod health endpoint, and would distinguish AMBER.
 * </p>
 */
@Service
@Slf4j
public class PodHealthProbe {

    private final PodRepository podRepository;

    private final PodHealthCheckRepository healthCheckRepository;

    private final RestClient probeClient;

    public PodHealthProbe(PodRepository podRepository, PodHealthCheckRepository healthCheckRepository,
                          @Value("${cvhome.pod-registry.health.timeout:PT3S}") Duration timeout) {
        this.podRepository = podRepository;
        this.healthCheckRepository = healthCheckRepository;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // A probe that can hang is worse than no probe: it would hold the scheduler thread and make every pod look
        // fine because none of them ever finished being checked.
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        this.probeClient = RestClient.builder().requestFactory(factory).build();
    }

    @Scheduled(fixedRateString = "${cvhome.pod-registry.health.rate:PT1M}")
    public void probeAll() {
        podRepository.findAll().forEach(this::probe);
    }

    /**
     * One pod's probe, in its own transaction so a failure to record one does not abandon the rest of the sweep.
     */
    @Transactional
    public void probe(PodEntity pod) {
        Instant started = Instant.now();
        PodHealthStatus status;
        String detail;
        try {
            probeClient.get().uri(pod.getEndpoint()).retrieve().toBodilessEntity();
            status = PodHealthStatus.GREEN;
            detail = null;
        } catch (RestClientResponseException e) {
            // The pod answered, with an error status. It is up; something behind it may not be, which is not what
            // this probe is for.
            status = PodHealthStatus.GREEN;
            detail = String.format("answered %s", e.getStatusCode());
        } catch (Exception e) {
            status = PodHealthStatus.RED;
            detail = e.getClass().getSimpleName();
            log.warn("Pod {} ({}) is unreachable: {}", pod.getName(), pod.getEndpoint(), detail);
        }
        int latencyMs = (int) Duration.between(started, Instant.now()).toMillis();
        record(pod, status, latencyMs, detail);
    }

    private void record(PodEntity pod, PodHealthStatus status, int latencyMs, String detail) {
        PodHealthStatus previous = pod.getLastHealthStatus();
        pod.setLastHealthStatus(status);
        pod.setLastHealthAt(Instant.now());
        podRepository.save(pod);
        healthCheckRepository.save(PodHealthCheckEntity.of(pod.getId(), status, latencyMs, detail));
        if (previous != status) {
            log.info("Pod {} health {} -> {}", pod.getName(), previous, status);
        }
    }

}
