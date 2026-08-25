package com.asrevo.cvhome.podregistry.config;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.asrevo.cvhome.commons.domain.EndpointType;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodEndpoint;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.podregistry.commons.errors.DuplicatePodNameException;
import com.asrevo.cvhome.podregistry.domain.PodEntity;
import com.asrevo.cvhome.podregistry.repository.PodRepository;
import com.asrevo.cvhome.podregistry.service.PodService;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * The seed is an upsert and never a delete, it runs under an advisory lock, and nothing in it may fail the boot —
 * the registry is the gateway's route source, so a start-up that refuses is worse than a stale row.
 */
class PodSeedInitializerTest {

    private static final PodId KNOWN = new PodId("507f1f77bcf86cd799439011");

    private static final PodId NEW = new PodId("507f1f77bcf86cd799439012");

    private static final PodEndpoint ENDPOINT = new PodEndpoint("http://spg.example", EndpointType.EXTERNAL);

    private PodRepository repository;

    private PodService podService;

    private static Pod pod(PodId id, String name) {
        return new Pod(id, name, ENDPOINT, null, null);
    }

    private static PodSeedInitializer initializer(PodRepository repository, PodService podService, List<Pod> pods) {
        return new PodSeedInitializer(new ServiceDomainProperties(Map.of(), pods), repository, podService);
    }

    @BeforeEach
    void setUp() {
        repository = mock(PodRepository.class);
        podService = mock(PodService.class);
        when(repository.tryLockForSeeding(anyLong())).thenReturn(true);
    }

    @Test
    void noConfiguredPodsLeavesTheRegistryAlone() {
        initializer(repository, podService, null).onApplicationReady();
        initializer(repository, podService, List.of()).onApplicationReady();

        verifyNoInteractions(repository, podService);
    }

    @Test
    void anotherInstanceHoldingTheLockMeansSkip() {
        when(repository.tryLockForSeeding(anyLong())).thenReturn(false);

        initializer(repository, podService, List.of(pod(NEW, "pod-new"))).onApplicationReady();

        verify(repository, never()).save(any());
        verifyNoInteractions(podService);
    }

    @Test
    void unknownPodsAreInsertedAndKnownOnesRefreshed() throws Exception {
        when(repository.findById(NEW)).thenReturn(Optional.empty());
        when(repository.findById(KNOWN)).thenReturn(Optional.of(new PodEntity()));
        Pod fresh = pod(NEW, "pod-fresh");
        Pod known = pod(KNOWN, "pod-known");
        Pod idless = pod(null, "pod-idless");

        initializer(repository, podService, List.of(fresh, known, idless)).onApplicationReady();

        ArgumentCaptor<PodEntity> saved = ArgumentCaptor.forClass(PodEntity.class);
        verify(repository, times(2)).save(saved.capture());
        assertThat(saved.getAllValues()).extracting(PodEntity::getId).contains(NEW).doesNotContain(KNOWN);
        verify(podService).update(KNOWN, known);
        verify(podService, never()).update(NEW, fresh);
    }

    @Test
    @DisplayName("an id-less configured pod is inserted with a generated id rather than looked up")
    void idlessPodIsInsertedWithoutLookup() {
        Pod idless = pod(null, "pod-anon");

        initializer(repository, podService, List.of(idless)).onApplicationReady();

        ArgumentCaptor<PodEntity> saved = ArgumentCaptor.forClass(PodEntity.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getId()).isNotNull();
        verify(repository, never()).findById(any());
    }

    @Test
    void aPodThatCannotBeReconciledDoesNotStopTheOthers() throws Exception {
        Pod clashing = pod(KNOWN, "pod-clash");
        Pod fine = pod(NEW, "pod-fine");
        when(repository.findById(KNOWN)).thenReturn(Optional.of(new PodEntity()));
        when(repository.findById(NEW)).thenReturn(Optional.empty());
        when(podService.update(KNOWN, clashing)).thenThrow(DuplicatePodNameException.of(clashing.name()));

        assertThatCode(() -> initializer(repository, podService, List.of(clashing, fine)).onApplicationReady())
                .doesNotThrowAnyException();

        verify(repository).save(any());
    }

    @Test
    void anInfrastructureFailureNeverFailsTheBoot() {
        when(repository.tryLockForSeeding(anyLong())).thenThrow(new IllegalStateException("db down"));

        assertThatCode(() -> initializer(repository, podService, List.of(pod(NEW, "pod-x"))).onApplicationReady())
                .doesNotThrowAnyException();
    }

}
