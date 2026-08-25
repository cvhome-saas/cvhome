package com.asrevo.cvhome.tenancy.commons.dto;

import com.asrevo.cvhome.commons.domain.PodId;

/**
 * How many stores tenancy has placed on one pod.
 *
 * <p>
 * The authoritative count: tenancy owns {@code manager_store.pod_id}. Pod-registry keeps its own
 * {@code pod.capacity_stores}, but that is a mirror maintained from tenancy's outbox and it only knows about stores
 * placed through it — which is why the two can disagree, and why this exists.
 * </p>
 *
 * <p>
 * A pod with no stores does not appear in the list at all rather than appearing as zero: the query groups, and
 * there is nothing to group. Callers read a missing pod as none placed.
 * </p>
 *
 * @param podId  the pod
 * @param stores how many non-deleted stores are on it
 */
public record PodStoreCount(PodId podId, long stores) {
}
