package com.asrevo.cvhome.podregistry.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.podregistry.commons.PodHealthStatus;
import com.asrevo.cvhome.podregistry.commons.PodLifecycleState;
import com.asrevo.cvhome.podregistry.commons.dto.PlacementDecision;
import com.asrevo.cvhome.podregistry.commons.dto.PlacementRequest;
import com.asrevo.cvhome.podregistry.commons.errors.NoEligiblePodException;
import com.asrevo.cvhome.podregistry.domain.PodEntity;
import com.asrevo.cvhome.podregistry.repository.PodRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Decides which pod a new store should be created on.
 *
 * <p>
 * This replaces tenancy's {@code PodSelectionImpl}, which had a cross-tenant defect worth restating so it is not
 * reintroduced: when an organization had no private pod, it asked for "public" pods through a method that returned
 * <em>every</em> pod, so a store could be placed onto <strong>another organization's private pod</strong>. The
 * candidate sets below are disjoint by construction and there is no path from the private branch to the public one.
 * </p>
 *
 * <p>
 * Placement writes nothing. It is asked while the caller is still deciding whether to create the store at all, so a
 * reservation could be abandoned and would leak capacity; counting happens in phase 8 from the store events that
 * follow a creation that actually happened.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PodPlacementService {

    private final PodRepository podRepository;

    /**
     * @throws NoEligiblePodException nothing can take the store — including the case where the organization owns
     *                                private pods but none of them is currently eligible
     */
    @Transactional(readOnly = true)
    public PlacementDecision place(PlacementRequest request) throws NoEligiblePodException {
        ManagerOrgId org = request.org();
        List<PodEntity> privatePods = Objects.isNull(org) || Objects.isNull(org.id()) ? List.of()
                : podRepository.findAllByOrgId(org);

        if (!privatePods.isEmpty()) {
            return placeOnDedicated(request, privatePods);
        }
        return placeOnShared(request);
    }

    /**
     * An organization with dedicated pods is confined to them.
     *
     * <p>
     * If none is eligible this refuses rather than widening the search. Falling back to shared infrastructure would
     * be the same class of bug this service was written to remove, and it would be silent — the store would work,
     * on hardware its owner did not agree to.
     * </p>
     */
    private PlacementDecision placeOnDedicated(PlacementRequest request, List<PodEntity> privatePods)
            throws NoEligiblePodException {
        List<PodEntity> eligible = privatePods.stream().filter(PodPlacementService::isEligible).toList();
        if (eligible.isEmpty()) {
            log.warn("Org {} has {} private pod(s) but none is eligible; refusing rather than using a shared pod",
                    request.org().id(), privatePods.size());
            throw NoEligiblePodException.of(request.org().id(),
                    "the organization's own pods are all draining, unhealthy or full");
        }
        PodEntity preferred = preferred(request.preferredPodId(), eligible);
        if (Objects.nonNull(preferred)) {
            return new PlacementDecision(preferred.getId(), true, "requested private pod");
        }
        return new PlacementDecision(leastLoaded(eligible).getId(), true, "least-loaded private pod");
    }

    private PlacementDecision placeOnShared(PlacementRequest request) throws NoEligiblePodException {
        List<PodEntity> eligible = podRepository.findPlaceablePublicPods()
                .stream()
                .filter(PodPlacementService::isEligible)
                .toList();
        if (eligible.isEmpty()) {
            throw NoEligiblePodException.of(Objects.isNull(request.org()) ? null : request.org().id(),
                    "no shared pod is active, healthy and under its capacity");
        }
        PodEntity preferred = preferred(request.preferredPodId(), eligible);
        if (Objects.nonNull(preferred)) {
            return new PlacementDecision(preferred.getId(), false, "requested shared pod");
        }
        return new PlacementDecision(leastLoaded(eligible).getId(), false, "least-loaded shared pod");
    }

    /** A preference is honoured only from the candidate set, never as a way around it. */
    private PodEntity preferred(PodId preferredPodId, List<PodEntity> eligible) {
        if (Objects.isNull(preferredPodId)) {
            return null;
        }
        return eligible.stream().filter(it -> preferredPodId.equals(it.getId())).findFirst().orElse(null);
    }

    /**
     * Fullest-last, by fraction of capacity used so a small pod and a large one are compared fairly. An uncapped pod
     * sorts as empty, which is the intent: it has no ceiling to be near.
     *
     * <p>
     * The selector this replaces picked at random from a shared, unsynchronised {@code Random} — which spread stores
     * evenly across pods of wildly different sizes and could hand out a pod that was already full.
     * </p>
     */
    private PodEntity leastLoaded(List<PodEntity> eligible) {
        return eligible.stream().min(Comparator.comparingDouble(PodPlacementService::load)).orElseThrow();
    }

    private static double load(PodEntity pod) {
        Integer max = pod.getCapacityMaxStores();
        if (Objects.isNull(max) || max <= 0) {
            return 0d;
        }
        return (double) pod.getCapacityStores() / max;
    }

    /**
     * Health gates placement, never routing — a RED pod keeps its gateway route because its existing tenants live
     * there. A pod that has never been probed is treated as eligible: until phase 8 nothing probes at all, and
     * refusing on "unknown" would mean no store could ever be created.
     */
    private static boolean isEligible(PodEntity pod) {
        boolean active = pod.getLifecycleState() == PodLifecycleState.ACTIVE;
        boolean healthy = Objects.isNull(pod.getLastHealthStatus())
                || pod.getLastHealthStatus() == PodHealthStatus.GREEN;
        Integer max = pod.getCapacityMaxStores();
        boolean hasRoom = Objects.isNull(max) || pod.getCapacityStores() < max;
        return active && healthy && hasRoom;
    }

}
