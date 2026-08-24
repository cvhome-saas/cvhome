package com.asrevo.cvhome.inventory.model.reservation;

import java.io.Serializable;
import java.time.Instant;

/**
 * Outcome of a release call. {@code status} is false when there was nothing to release — no reservation under that
 * ref, or one already committed.
 */
public record ProductReservationReleaseResult(boolean status, Long reservationId,
                                              Instant expireAt) implements Serializable {
}
