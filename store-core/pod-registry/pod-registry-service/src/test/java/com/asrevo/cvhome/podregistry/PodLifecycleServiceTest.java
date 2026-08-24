package com.asrevo.cvhome.podregistry;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.asrevo.cvhome.commons.domain.EndpointType;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodEndpoint;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.podregistry.commons.AuditSource;
import com.asrevo.cvhome.podregistry.commons.PodLifecycleState;
import com.asrevo.cvhome.podregistry.commons.errors.PodNotFoundException;
import com.asrevo.cvhome.podregistry.domain.PodAuditEntity;
import com.asrevo.cvhome.podregistry.domain.PodEntity;
import com.asrevo.cvhome.podregistry.repository.PodAuditRepository;
import com.asrevo.cvhome.podregistry.repository.PodRepository;
import com.asrevo.cvhome.podregistry.service.PodLifecycleService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PodLifecycleServiceTest {

    private static final PodId POD = new PodId("507f1f77bcf86cd799439011");

    private static final String ACTOR = "super-admin";

    private PodRepository podRepository;

    private PodAuditRepository auditRepository;

    private PodLifecycleService service;

    private static PodEntity activePod() {
        PodEntity entity = PodEntity.newEntity(
                new Pod(POD, "pod-507f1f77", new PodEndpoint("http://x", EndpointType.EXTERNAL), null, null));
        entity.setLifecycleState(PodLifecycleState.ACTIVE);
        return entity;
    }

    @BeforeEach
    void setUp() {
        podRepository = mock(PodRepository.class);
        auditRepository = mock(PodAuditRepository.class);
        when(podRepository.save(any())).thenAnswer(it -> it.getArgument(0));
        service = new PodLifecycleService(podRepository, auditRepository);
    }

    @Test
    @DisplayName("draining moves the pod out of rotation and records who did it")
    void drainAudits() throws PodNotFoundException {
        when(podRepository.findById(POD)).thenReturn(Optional.of(activePod()));

        assertThat(service.drain(POD, ACTOR).lifecycleState()).isEqualTo(PodLifecycleState.DRAINING);

        ArgumentCaptor<PodAuditEntity> audit = ArgumentCaptor.forClass(PodAuditEntity.class);
        verify(auditRepository).save(audit.capture());
        assertThat(audit.getValue().getFromLifecycle()).isEqualTo(PodLifecycleState.ACTIVE);
        assertThat(audit.getValue().getToLifecycle()).isEqualTo(PodLifecycleState.DRAINING);
        assertThat(audit.getValue().getActor()).isEqualTo(ACTOR);
        assertThat(audit.getValue().getSource()).isEqualTo(AuditSource.API);
    }

    @Test
    @DisplayName("a drained pod is no longer eligible for placement, but is still a pod")
    void drainedPodIsNotActive() throws PodNotFoundException {
        when(podRepository.findById(POD)).thenReturn(Optional.of(activePod()));

        // PodPlacementService only considers ACTIVE pods; routing does not consult lifecycle at all, which is what
        // keeps a drained pod's existing storefronts working.
        assertThat(service.drain(POD, ACTOR).openToPlacement()).isFalse();
    }

    @Test
    @DisplayName("resuming puts it back")
    void resumeRestores() throws PodNotFoundException {
        PodEntity draining = activePod();
        draining.setLifecycleState(PodLifecycleState.DRAINING);
        when(podRepository.findById(POD)).thenReturn(Optional.of(draining));

        assertThat(service.resume(POD, ACTOR).lifecycleState()).isEqualTo(PodLifecycleState.ACTIVE);
    }

    @Test
    @DisplayName("draining twice is a no-op that still leaves a trace of the second request")
    void repeatedDrainIsAuditedButChangesNothing() throws PodNotFoundException {
        PodEntity draining = activePod();
        draining.setLifecycleState(PodLifecycleState.DRAINING);
        when(podRepository.findById(POD)).thenReturn(Optional.of(draining));

        service.drain(POD, ACTOR);

        verify(podRepository, never()).save(any());
        verify(auditRepository).save(any());
    }

    @Test
    @DisplayName("draining an unknown pod is a typed 404")
    void unknownPodIsNotFound() {
        when(podRepository.findById(POD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.drain(POD, ACTOR)).isInstanceOf(PodNotFoundException.class);
    }

}
