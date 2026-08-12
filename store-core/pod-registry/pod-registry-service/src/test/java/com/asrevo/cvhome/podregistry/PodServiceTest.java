package com.asrevo.cvhome.podregistry;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import com.asrevo.cvhome.commons.domain.EndpointType;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodEndpoint;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.podregistry.commons.PodLifecycleState;
import com.asrevo.cvhome.podregistry.commons.PodVisibility;
import com.asrevo.cvhome.podregistry.commons.errors.DuplicatePodNameException;
import com.asrevo.cvhome.podregistry.commons.errors.PodNotFoundException;
import com.asrevo.cvhome.podregistry.domain.PodEntity;
import com.asrevo.cvhome.podregistry.repository.PodRepository;
import com.asrevo.cvhome.podregistry.service.PodServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit-test")
class PodServiceTest {

    private static final PodId POD = new PodId("507f1f77bcf86cd799439011");

    private static final ManagerOrgId ORG = new ManagerOrgId("21f023932bc66470c104b76f");

    private static final String NAME = "pod-a";

    private PodRepository repository;

    private PodServiceImpl service;

    private static Pod pod(String name) {
        return new Pod(null, name, new PodEndpoint("http://pod.example", EndpointType.EXTERNAL), null, null);
    }

    @BeforeEach
    void setUp() {
        repository = mock(PodRepository.class);
        service = new PodServiceImpl(repository);
    }

    @Test
    @DisplayName("an unknown pod is a typed 404, where the registry used to answer 200 with a null body")
    void unknownPodIsNotFound() {
        when(repository.findById(POD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.pod(POD)).isInstanceOf(PodNotFoundException.class);
    }

    @Test
    @DisplayName("a duplicate name is refused before the insert, with a code naming the pod")
    void duplicateNameIsRefusedUpFront() {
        when(repository.findByName(NAME)).thenReturn(Optional.of(new PodEntity()));

        assertThatThrownBy(() -> service.save(pod(NAME))).isInstanceOf(DuplicatePodNameException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("losing the name race against another instance is the same typed 409, not a raw constraint error")
    void lostNameRaceIsTranslated() {
        when(repository.findByName(NAME)).thenReturn(Optional.empty());
        when(repository.save(any())).thenThrow(new DuplicateKeyException("pod_name_uq"));

        assertThatThrownBy(() -> service.save(pod(NAME))).isInstanceOf(DuplicatePodNameException.class);
    }

    @Test
    @DisplayName("placement candidates come from a real predicate, never the whole inventory")
    void placementUsesTheNarrowQuery() {
        when(repository.findPlaceablePublicPods()).thenReturn(List.of());

        assertThat(service.listPlaceablePublicPods()).isEmpty();

        // The bug being designed out: the registry this replaces answered listPublicPods() with findAll(), so an
        // org with no private pod could be placed onto another org's private pod.
        verify(repository).findPlaceablePublicPods();
        verify(repository, never()).findAll();
    }

    @Test
    @DisplayName("a pod that names an owner is private; one that does not is public")
    void visibilityFollowsOwnership() {
        PodEntity owned = PodEntity
                .newEntity(new Pod(POD, "p", new PodEndpoint("http://x", EndpointType.EXTERNAL), ORG, null));
        PodEntity shared = PodEntity.newEntity(pod("q"));

        assertThat(owned.getVisibility()).isEqualTo(PodVisibility.PRIVATE);
        assertThat(shared.getVisibility()).isEqualTo(PodVisibility.PUBLIC);
        assertThat(shared.getLifecycleState()).isEqualTo(PodLifecycleState.ACTIVE);
    }

}
