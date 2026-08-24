package com.asrevo.cvhome.inventory.model.reservation;

import java.io.Serializable;
import java.time.Instant;

import lombok.Builder;

@Builder
public record ProductReservationCommitResult(boolean status, Long reservationId,
                                             Instant expireAt) implements Serializable {
}
