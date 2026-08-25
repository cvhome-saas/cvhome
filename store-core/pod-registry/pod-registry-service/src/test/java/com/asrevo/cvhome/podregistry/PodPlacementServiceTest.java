package com.asrevo.cvhome.podregistry;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.EndpointType;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodEndpoint;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.podregistry.commons.PodHealthStatus;
import com.asrevo.cvhome.podregistry.commons.PodLifecycleState;
import com.asrevo.cvhome.podregistry.commons.dto.PlacementDecision;
import com.asrevo.cvhome.podregistry.commons.dto.PlacementRequest;
import com.asrevo.cvhome.podregistry.commons.errors.NoEligiblePodException;
import com.asrevo.cvhome.podregistry.domain.PodEntity;
import com.asrevo.cvhome.podregistry.repository.PodRepository;
import com.asrevo.cvhome.podregistry.service.PodPlacementService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The placement rules, and above all the one this service exists to remove.
 *
 * <p>
 * Tenancy's {@code PodSelectionImpl} asked for "public" pods through a method that returned every pod, so an
 * organization with no private pod of its own could have a store placed on <em>another organization's private
 * pod</em>. {@link #neverFallsBackToAnotherOrgsPod()} and {@link #privateOrgWithNoEligiblePodIsRefused()} are the
 * two halves of that being impossible now.
 * </p>
 */
class PodPlacementServiceTest {

    private static final ManagerOrgId ORG = new ManagerOrgId("21f023932bc66470c104b76f");

    private static final ManagerOrgId OTHER_ORG = new ManagerOrgId("31f023932bc66470c104b770");

    private static final PodId PRIVATE_POD = new PodId("507f1f77bcf86cd799439011");

    private static final PodId SHARED_POD = new PodId("607f1f77bcf86cd799439012");

    private static final PodId BIG_SHARED_POD = new PodId("707f1f77bcf86cd799439013");

    private PodRepository repository;

    private PodPlacementService service;

    private static PodEntity pod(PodId id, ManagerOrgId owner) {
        PodEntity entity = PodEntity
                .newEntity(new Pod(id, String.format("pod-%s", id.shorten()),
                        new PodEndpoint("http://x", EndpointType.EXTERNAL), owner, null));
        entity.setLifecycleState(PodLifecycleState.ACTIVE);
        return entity;
    }

    private static PodEntity withCapacity(PodEntity pod, int used, Integer max) {
        pod.setCapacityStores(used);
        pod.setCapacityMaxStores(max);
        return pod;
    }

    @BeforeEach
    void setUp() {
        repository = mock(PodRepository.class);
        service = new PodPlacementService(repository);
    }

    @Test
    @DisplayName("an org with a private pod always lands on it")
    void privatePodWins() throws NoEligiblePodException {
        when(repository.findAllByOrgId(ORG)).thenReturn(List.of(pod(PRIVATE_POD, ORG)));

        PlacementDecision decision = service.place(new PlacementRequest(ORG));

        assertThat(decision.podId()).isEqualTo(PRIVATE_POD);
        assertThat(decision.dedicated()).isTrue();
        // The shared pool is never even consulted for an org that has its own.
        verify(repository, never()).findPlaceablePublicPods();
    }

    @Test
    @DisplayName("an org whose private pods are all ineligible is refused, never moved onto shared infrastructure")
    void privateOrgWithNoEligiblePodIsRefused() {
        PodEntity draining = pod(PRIVATE_POD, ORG);
        draining.setLifecycleState(PodLifecycleState.DRAINING);
        when(repository.findAllByOrgId(ORG)).thenReturn(List.of(draining));

        assertThatThrownBy(() -> service.place(new PlacementRequest(ORG)))
                .isInstanceOf(NoEligiblePodException.class);

        // This is the whole point: no fallback query is issued, so a shared pod cannot be substituted.
        verify(repository, never()).findPlaceablePublicPods();
    }

    @Test
    @DisplayName("an org with no private pod never receives another org's private pod")
    void neverFallsBackToAnotherOrgsPod() throws NoEligiblePodException {
        when(repository.findAllByOrgId(ORG)).thenReturn(List.of());
        // findPlaceablePublicPods is a real predicate (visibility = PUBLIC and org_id is null), so another org's
        // private pod is excluded in the query rather than filtered here.
        when(repository.findPlaceablePublicPods()).thenReturn(List.of(pod(SHARED_POD, null)));

        PlacementDecision decision = service.place(new PlacementRequest(ORG));

        assertThat(decision.podId()).isEqualTo(SHARED_POD);
        assertThat(decision.dedicated()).isFalse();
        verify(repository, never()).findAll();
    }

    @Test
    @DisplayName("a draining shared pod is skipped but a healthy one is used")
    void drainingSharedPodIsSkipped() throws NoEligiblePodException {
        PodEntity draining = pod(SHARED_POD, null);
        draining.setLifecycleState(PodLifecycleState.DRAINING);
        when(repository.findAllByOrgId(ORG)).thenReturn(List.of());
        when(repository.findPlaceablePublicPods()).thenReturn(List.of(draining, pod(BIG_SHARED_POD, null)));

        assertThat(service.place(new PlacementRequest(ORG)).podId()).isEqualTo(BIG_SHARED_POD);
    }

    @Test
    @DisplayName("a full pod is skipped; an uncapped pod is never full")
    void capacityIsRespected() throws NoEligiblePodException {
        when(repository.findAllByOrgId(ORG)).thenReturn(List.of());
        when(repository.findPlaceablePublicPods())
                .thenReturn(List.of(withCapacity(pod(SHARED_POD, null), 10, 10),
                        withCapacity(pod(BIG_SHARED_POD, null), 500, null)));

        assertThat(service.place(new PlacementRequest(ORG)).podId()).isEqualTo(BIG_SHARED_POD);
    }

    @Test
    @DisplayName("ties break to the least loaded by fraction, not at random")
    void leastLoadedWins() throws NoEligiblePodException {
        when(repository.findAllByOrgId(ORG)).thenReturn(List.of());
        when(repository.findPlaceablePublicPods())
                .thenReturn(List.of(withCapacity(pod(SHARED_POD, null), 9, 10),
                        withCapacity(pod(BIG_SHARED_POD, null), 100, 1000)));

        // 90% full versus 10% full — the bigger absolute count is the emptier pod.
        assertThat(service.place(new PlacementRequest(ORG)).podId()).isEqualTo(BIG_SHARED_POD);
    }

    @Test
    @DisplayName("an unhealthy pod is excluded, but a never-probed one is not")
    void healthGatesPlacement() throws NoEligiblePodException {
        PodEntity red = pod(SHARED_POD, null);
        red.setLastHealthStatus(PodHealthStatus.RED);
        when(repository.findAllByOrgId(ORG)).thenReturn(List.of());
        when(repository.findPlaceablePublicPods()).thenReturn(List.of(red, pod(BIG_SHARED_POD, null)));

        // The second pod has never been probed (null health) and is still eligible — until phase 8 nothing probes,
        // so refusing on "unknown" would mean no store could ever be created.
        assertThat(service.place(new PlacementRequest(ORG)).podId()).isEqualTo(BIG_SHARED_POD);
    }

    @Test
    @DisplayName("a preferred pod is honoured only from the candidate set")
    void preferenceCannotEscapeTheCandidateSet() throws NoEligiblePodException {
        when(repository.findAllByOrgId(ORG)).thenReturn(List.of());
        when(repository.findPlaceablePublicPods()).thenReturn(List.of(pod(SHARED_POD, null)));

        // Asking for another org's private pod by id does not get it: it is not in the candidate set, so the
        // preference is ignored and the normal choice is made.
        PlacementDecision decision = service.place(new PlacementRequest(ORG, PRIVATE_POD));

        assertThat(decision.podId()).isEqualTo(SHARED_POD);
    }

    @Test
    @DisplayName("nothing available is a typed 422, not the bound-must-be-positive 500 it used to be")
    void nothingAvailableIsTyped() {
        when(repository.findAllByOrgId(ORG)).thenReturn(List.of());
        when(repository.findPlaceablePublicPods()).thenReturn(List.of());

        assertThatThrownBy(() -> service.place(new PlacementRequest(ORG)))
                .isInstanceOf(NoEligiblePodException.class);
    }

    @Test
    @DisplayName("another org's pods are looked up under that org, never mixed with ours")
    void lookupIsPerOrg() throws NoEligiblePodException {
        when(repository.findAllByOrgId(OTHER_ORG)).thenReturn(List.of(pod(PRIVATE_POD, OTHER_ORG)));

        assertThat(service.place(new PlacementRequest(OTHER_ORG)).podId()).isEqualTo(PRIVATE_POD);
        verify(repository).findAllByOrgId(OTHER_ORG);
        verify(repository, never()).findAllByOrgId(ORG);
    }

}
