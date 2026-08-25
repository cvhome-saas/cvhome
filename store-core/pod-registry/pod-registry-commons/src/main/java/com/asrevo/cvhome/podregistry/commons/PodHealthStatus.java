package com.asrevo.cvhome.podregistry.commons;

/**
 * The result of the most recent health probe.
 *
 * <p>
 * Health gates <em>placement only</em>. A RED pod keeps its gateway route: its tenants already live there, so
 * withdrawing the route converts "degraded" into "entirely offline".
 * </p>
 */
public enum PodHealthStatus {

    GREEN,
    AMBER,
    RED

}
