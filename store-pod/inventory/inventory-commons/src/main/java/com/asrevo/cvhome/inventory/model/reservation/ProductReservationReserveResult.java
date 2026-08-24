package com.asrevo.cvhome.inventory.model.reservation;

import java.io.Serializable;
import java.time.Instant;

/**
 * Outcome of a reserve call. Stock was taken; it is held until {@code expireAt} unless committed.
 */
public record ProductReservationReserveResult(boolean status, Long reservationId,
                                              Instant expireAt) implements Serializable {
}
