package com.asrevo.cvhome.podregistry.commons;

/**
 * Where a pod is in its operational life.
 *
 * <p>
 * Only {@link #ACTIVE} is eligible to receive new stores. {@link #DRAINING} and {@link #DECOMMISSIONED} are
 * deliberately still <em>routed</em> — the stores already on them are live, and taking their route away breaks
 * working storefronts to fix nothing. Lifecycle gates placement, never routing.
 * </p>
 */
public enum PodLifecycleState {

    /** Registered but not yet ready to take stores. */
    PROVISIONING,

    /** Healthy and open for placement. */
    ACTIVE,

    /** Still serving its stores, but no new store may be placed here. */
    DRAINING,

    /** Retired. Kept as a row because stores and audit rows still reference it. */
    DECOMMISSIONED

}
