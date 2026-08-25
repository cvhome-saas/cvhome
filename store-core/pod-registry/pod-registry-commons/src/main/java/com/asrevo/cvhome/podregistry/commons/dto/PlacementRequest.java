package com.asrevo.cvhome.podregistry.commons.dto;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.PodId;

/**
 * Asks the registry where a new store for this organization should live.
 *
 * <p>
 * Carries the org rather than the store because the store does not exist yet — this is asked while deciding whether
 * it can be created at all. That is also why placement is a pure decision and writes nothing: the caller may still
 * abandon the creation, and a reservation nobody released would leak capacity.
 * </p>
 *
 * @param org           whose store this will be; decides which pods are even candidates
 * @param preferredPodId an operator's choice, honoured only if that pod is one the org may use and is eligible.
 *                       Null means no preference
 */
public record PlacementRequest(ManagerOrgId org, PodId preferredPodId) {

    public PlacementRequest(ManagerOrgId org) {
        this(org, null);
    }

}
