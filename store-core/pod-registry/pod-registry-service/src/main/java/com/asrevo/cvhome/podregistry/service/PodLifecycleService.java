package com.asrevo.cvhome.podregistry.service;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.podregistry.commons.AuditSource;
import com.asrevo.cvhome.podregistry.commons.PodLifecycleState;
import com.asrevo.cvhome.podregistry.commons.dto.PodView;
import com.asrevo.cvhome.podregistry.commons.errors.PodNotFoundException;
import com.asrevo.cvhome.podregistry.domain.PodAuditEntity;
import com.asrevo.cvhome.podregistry.domain.PodEntity;
import com.asrevo.cvhome.podregistry.repository.PodAuditRepository;
import com.asrevo.cvhome.podregistry.repository.PodRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Moves a pod through its lifecycle, and records who moved it.
 *
 * <p>
 * Draining is the safe counterpart to deleting. A drained pod takes no new stores but keeps serving the ones it
 * has and keeps its gateway route — the tenants living there are unaffected, which is the whole difference between
 * retiring a pod and breaking it. Deleting, by contrast, strands every store on it and has no undo.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PodLifecycleService {

    private final PodRepository podRepository;

    private final PodAuditRepository auditRepository;

    /** Stop placing new stores here. Existing stores and routing are untouched. */
    @Transactional
    public PodView drain(PodId podId, String actor) throws PodNotFoundException {
        return transition(podId, PodLifecycleState.DRAINING, actor, "drained by operator");
    }

    /** Put a drained pod back into rotation. */
    @Transactional
    public PodView resume(PodId podId, String actor) throws PodNotFoundException {
        return transition(podId, PodLifecycleState.ACTIVE, actor, "returned to rotation by operator");
    }

    /**
     * A transition to the state a pod is already in is a no-op that still audits.
     *
     * <p>
     * Deliberate: draining twice is not an error worth failing a request over, and the audit row is the useful part
     * — it records that someone asked, which is what you want when reconstructing an incident.
     * </p>
     */
    private PodView transition(PodId podId, PodLifecycleState to, String actor, String detail)
            throws PodNotFoundException {
        PodEntity pod = podRepository.findById(podId).orElseThrow(() -> PodNotFoundException.of(podId));
        PodLifecycleState from = pod.getLifecycleState();
        if (Objects.equals(from, to)) {
            log.info("Pod {} is already {}; recording the request and changing nothing", podId, to);
            auditRepository.save(PodAuditEntity.of(podId, from, to, AuditSource.API, actor,
                    String.format("no-op: %s", detail)));
            return pod.toView();
        }
        pod.setLifecycleState(to);
        PodEntity saved = podRepository.save(pod);
        auditRepository.save(PodAuditEntity.of(podId, from, to, AuditSource.API, actor, detail));
        log.info("Pod {} moved {} -> {} by {}", podId, from, to, actor);
        return saved.toView();
    }

}
