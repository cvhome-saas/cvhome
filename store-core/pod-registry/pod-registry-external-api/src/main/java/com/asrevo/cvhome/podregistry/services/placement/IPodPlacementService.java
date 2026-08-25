package com.asrevo.cvhome.podregistry.services.placement;

import com.asrevo.cvhome.podregistry.commons.dto.PlacementDecision;
import com.asrevo.cvhome.podregistry.commons.dto.PlacementRequest;
import com.asrevo.cvhome.podregistry.commons.errors.NoEligiblePodException;

/**
 * The placement contract in the registry's own vocabulary.
 *
 * <p>
 * Implemented by pod-registry's controller, which is why the {@code throws} clause names the server-side exception.
 * Callers depend on {@link ExternalPodPlacementService} instead, whose clauses are the caller's truth.
 * </p>
 */
public interface IPodPlacementService {

    PlacementDecision place(PlacementRequest request) throws NoEligiblePodException;

}
