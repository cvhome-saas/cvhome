package com.asrevo.cvhome.podregistry.commons.dto;

import java.time.Instant;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.PodEndpoint;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.podregistry.commons.PodHealthStatus;
import com.asrevo.cvhome.podregistry.commons.PodLifecycleState;
import com.asrevo.cvhome.podregistry.commons.PodVisibility;

/**
 * A pod as this service describes it — everything the registry knows, including the operational state that only it
 * tracks.
 *
 * <p>
 * <strong>Deliberately not an extension of {@code com.asrevo.cvhome.commons.domain.Pod}.</strong> That record is on
 * every service's classpath and is the wire type the gateway builds routes from and {@code StorePodClientFactory}
 * resolves against; widening it would change a type five services deserialize in order to serve one service's
 * screens. {@code Pod} stays the minimal routing contract, and this carries the rest.
 * </p>
 */
public record PodView(PodId id,
                      String name,
                      PodEndpoint endpoint,
                      ManagerOrgId orgId,
                      PodVisibility visibility,
                      PodLifecycleState lifecycleState,
                      String region,
                      Integer capacityMaxStores,
                      int capacityStores,
                      PodHealthStatus lastHealthStatus,
                      Instant lastHealthAt) {

    /** Whether a new store may be placed here, ignoring org ownership and capacity. */
    public boolean openToPlacement() {
        return lifecycleState == PodLifecycleState.ACTIVE;
    }

    /** Whether the pod has room, treating a null ceiling as unlimited. */
    public boolean hasRoom() {
        return capacityMaxStores == null || capacityStores < capacityMaxStores;
    }

}
