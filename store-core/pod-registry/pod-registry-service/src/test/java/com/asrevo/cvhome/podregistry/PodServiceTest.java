package com.asrevo.cvhome.podregistry;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PodServiceTest {

    private static final PodId POD = new PodId("507f1f77bcf86cd799439011");

    private static final ManagerOrgId ORG = new ManagerOrgId("21f023932bc66470c104b76f");

    private static final String NAME = "pod-a";

    private static final String OTHER_NAME = "pod-b";

    private static final String ENDPOINT = "http://pod.example";

    private static final String MOVED_ENDPOINT = "http://moved";

    private PodRepository repository;

    private PodServiceImpl service;

    private static PodEntity entity(String name) {
        PodEntity entity = PodEntity.newEntity(new Pod(POD, name,
                new PodEndpoint(ENDPOINT, EndpointType.EXTERNAL), null, null));
        entity.setId(POD);
        return entity;
    }

    private static Pod pod(String name) {
        return new Pod(null, name, new PodEndpoint(ENDPOINT, EndpointType.EXTERNAL), null, null);
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

    @Test
    @DisplayName("the two list paths differ only in whether an owner narrows them")
    void listingIsScopedByOwnerWhenOneIsGiven() {
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity(NAME))));
        when(repository.findAllByOrgId(eq(ORG), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity(NAME))));

        assertThat(service.listAllPods(Pageable.unpaged())).hasSize(1);
        assertThat(service.listAllPods(ORG, Pageable.unpaged())).hasSize(1);

        verify(repository).findAll(any(Pageable.class));
        verify(repository).findAllByOrgId(eq(ORG), any(Pageable.class));
    }

    @Test
    @DisplayName("an unpaged search is capped rather than unbounded")
    void anUnpagedSearchFallsBackToTheDefaultPageSize() {
        when(repository.findVisible(any(), any(), anyInt(), anyLong())).thenReturn(List.of(entity(NAME)));
        when(repository.countVisible(any(), any())).thenReturn(1L);

        assertThat(service.searchPods(null, null, Pageable.unpaged())).hasSize(1);

        // Unbounded would hand the console every pod in the registry in one response.
        verify(repository).findVisible(null, null, 100, 0L);
    }

    @Test
    @DisplayName("a blank search term is no filter, not a filter matching nothing")
    void aBlankTermAndAnEmptyOwnerBecomeNulls() {
        when(repository.findVisible(any(), any(), anyInt(), anyLong())).thenReturn(List.of());
        when(repository.countVisible(any(), any())).thenReturn(0L);

        service.searchPods(new ManagerOrgId((String) null), "   ", PageRequest.of(1, 5));

        verify(repository).findVisible(null, null, 5, 5L);
    }

    @Test
    @DisplayName("a term is trimmed and the owner is passed as its hex")
    void aSearchNarrowsByOwnerAndTrimmedTerm() {
        when(repository.findVisible(any(), any(), anyInt(), anyLong())).thenReturn(List.of());
        when(repository.countVisible(any(), any())).thenReturn(0L);

        service.searchPods(ORG, "  pod  ", PageRequest.of(0, 10));

        verify(repository).findVisible(ORG.getId().toString(), "pod", 10, 0L);
    }

    @Test
    @DisplayName("view carries the registry's own fields, pod only the routing contract")
    void theTwoReadShapesAnswerDifferentAudiences() throws Exception {
        when(repository.findById(POD)).thenReturn(Optional.of(entity(NAME)));

        assertThat(service.pod(POD).name()).isEqualTo(NAME);
        assertThat(service.view(POD).visibility()).isEqualTo(PodVisibility.PUBLIC);
    }

    @Test
    @DisplayName("view is a typed 404 for an unknown pod, like pod()")
    void viewingAnUnknownPodIsAlsoNotFound() {
        when(repository.findById(POD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.view(POD)).isInstanceOf(PodNotFoundException.class);
    }

    @Test
    @DisplayName("renaming a pod onto a name another pod already holds is refused")
    void anUpdateThatCollidesWithAnotherPodsNameIsRefused() {
        PodEntity existing = entity(NAME);
        PodEntity other = entity(OTHER_NAME);
        other.setId(new PodId("507f1f77bcf86cd799439012"));
        when(repository.findById(POD)).thenReturn(Optional.of(existing));
        when(repository.findByName(OTHER_NAME)).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.update(POD, pod(OTHER_NAME)))
                .isInstanceOf(DuplicatePodNameException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("a pod keeping its own name is not a collision with itself")
    void anUpdateThatKeepsTheNameIsNotACollision() throws Exception {
        PodEntity existing = entity(NAME);
        when(repository.findById(POD)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(it -> it.getArgument(0));

        assertThat(service.update(POD, pod(NAME)).name()).isEqualTo(NAME);
        verify(repository, never()).findByName(any());
    }

    @Test
    @DisplayName("an update carries the new endpoint and its type together")
    void anUpdateReplacesTheEndpointAndItsType() throws Exception {
        PodEntity existing = entity(NAME);
        when(repository.findById(POD)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(it -> it.getArgument(0));

        Pod moved = new Pod(POD, NAME, new PodEndpoint(MOVED_ENDPOINT, EndpointType.INTERNAL), null, null);
        Pod saved = service.update(POD, moved);

        assertThat(saved.endpoint().endpoint()).isEqualTo(MOVED_ENDPOINT);
        assertThat(saved.endpoint().type()).isEqualTo(EndpointType.INTERNAL);
    }

    @Test
    @DisplayName("deleting an unknown pod is a typed 404 rather than a silent no-op")
    void deletingAnUnknownPodIsNotFound() {
        when(repository.findById(POD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(POD)).isInstanceOf(PodNotFoundException.class);
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("deleting a known pod removes the row it resolved")
    void deletingAKnownPodRemovesIt() throws Exception {
        PodEntity existing = entity(NAME);
        when(repository.findById(POD)).thenReturn(Optional.of(existing));

        service.delete(POD);

        verify(repository).delete(existing);
    }

}
