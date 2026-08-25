package com.asrevo.cvhome.podregistry.config;

import java.util.List;
import java.util.Objects;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.podregistry.commons.errors.DuplicatePodNameException;
import com.asrevo.cvhome.podregistry.commons.errors.PodNotFoundException;
import com.asrevo.cvhome.podregistry.domain.PodEntity;
import com.asrevo.cvhome.podregistry.repository.PodRepository;
import com.asrevo.cvhome.podregistry.service.PodService;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Reconciles the pods declared in configuration into the registry at start-up.
 *
 * <p>
 * The pod table is very close to a projection of {@code ServiceDomainProperties.pods()} — the only things it holds
 * that configuration cannot are the private-org assignment and whatever an operator created through the API. So the
 * seed is an upsert of the configured pods and never a delete: a pod an operator added by hand must survive a
 * restart, and a pod dropped from configuration must not silently take its stores' routing with it.
 * </p>
 *
 * <p>
 * <strong>Guarded by an advisory lock.</strong> This is read-then-write over pod names and it runs on every
 * instance at start-up; two instances booting together would race on {@code findByName} and one would lose on the
 * unique constraint. A transaction-level lock is released with the transaction, so a crash cannot wedge it.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PodSeedInitializer {

    /** Arbitrary but fixed: any instance seeding pods must pick the same number for the lock to mean anything. */
    private static final long SEED_LOCK_KEY = 8022_0001L;

    private final ServiceDomainProperties properties;

    private final PodRepository podRepository;

    private final PodService podService;

    /**
     * Nothing here may fail the boot.
     *
     * <p>
     * This service is where the gateway gets its route table, so a registry that refuses to start takes every
     * tenant storefront with it within one refresh period. A seed that cannot run leaves the stored rows exactly as
     * they were — which is a working registry, just not a freshly reconciled one — so the catch is deliberately the
     * base type and deliberately only logs.
     * </p>
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    @SuppressWarnings("java:S2221")
    public void onApplicationReady() {
        List<Pod> configured = properties.pods();
        if (Objects.isNull(configured) || configured.isEmpty()) {
            log.info("No pods declared in configuration; registry left as-is");
            return;
        }
        try {
            if (!podRepository.tryLockForSeeding(SEED_LOCK_KEY)) {
                log.info("Another instance is seeding the pod registry; skipping");
                return;
            }
            log.info("Reconciling {} configured pod(s) into the registry", configured.size());
            for (Pod pod : configured) {
                reconcile(pod);
            }
        } catch (Exception e) {
            log.error("Could not seed the pod registry; serving whatever is already stored", e);
        }
    }

    private void reconcile(Pod pod) {
        try {
            if (Objects.isNull(pod.id()) || podRepository.findById(pod.id()).isEmpty()) {
                podRepository.save(PodEntity.newEntity(pod));
                log.info("Seeded pod {} ({})", pod.name(), pod.id());
            } else {
                podService.update(pod.id(), pod);
                log.debug("Refreshed pod {} ({}) from configuration", pod.name(), pod.id());
            }
        } catch (PodNotFoundException | DuplicatePodNameException e) {
            // Neither is fatal to start-up: the registry keeps whatever it already had for this pod, and an
            // operator can reconcile by hand. Failing the boot would take the gateway's route source down with it.
            log.warn("Could not reconcile configured pod {} ({}); leaving the stored row alone", pod.name(),
                    pod.id(), e);
        }
    }

}
