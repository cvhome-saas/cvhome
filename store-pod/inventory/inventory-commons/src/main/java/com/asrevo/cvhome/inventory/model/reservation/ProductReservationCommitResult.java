package com.asrevo.cvhome.inventory.model.reservation;

import java.io.Serializable;
import java.time.Instant;

/**
 * Outcome of a commit call. {@code status} is false when there was nothing to commit — no reservation under that
 * ref, or one that had already expired or been released.
 */
public record ProductReservationCommitResult(boolean status, Long reservationId,
                                             Instant expireAt) implements Serializable {
}
