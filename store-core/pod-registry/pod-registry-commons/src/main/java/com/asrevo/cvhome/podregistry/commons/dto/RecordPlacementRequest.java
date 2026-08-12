package com.asrevo.cvhome.podregistry.commons.dto;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.PodId;

/**
 * Tells the registry that a store now actually lives on a pod.
 *
 * <p>
 * Separate from {@link PlacementRequest}, and deliberately so. Placement is a question asked while the caller is
 * still deciding; this is the answer to "did it happen", sent after the store row is committed. Keeping them apart
 * is what lets placement write nothing — a reservation made at decision time would leak capacity every time a
 * creation was abandoned.
 * </p>
 */
public record RecordPlacementRequest(ManagerStoreId store, PodId pod) {
}
