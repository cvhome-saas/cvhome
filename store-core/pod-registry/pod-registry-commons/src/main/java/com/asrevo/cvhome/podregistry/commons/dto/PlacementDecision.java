package com.asrevo.cvhome.podregistry.commons.dto;

import com.asrevo.cvhome.commons.domain.PodId;

/**
 * Where the registry says the store should go, and why that pod.
 *
 * <p>
 * The reason is for the operator reading logs after the fact, not for the caller to branch on — a caller that
 * needed to distinguish outcomes would be re-implementing the placement rules on the far side of the wire.
 * </p>
 *
 * @param podId    the chosen pod
 * @param dedicated whether it is the organization's own private pod rather than shared infrastructure
 * @param reason   short human-readable account of the choice
 */
public record PlacementDecision(PodId podId, boolean dedicated, String reason) {
}
