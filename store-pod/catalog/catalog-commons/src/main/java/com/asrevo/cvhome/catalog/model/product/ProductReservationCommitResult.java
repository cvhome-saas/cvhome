package com.asrevo.cvhome.catalog.model.product;

import java.io.Serializable;
import java.time.Instant;

import lombok.Builder;

@Builder
public record ProductReservationCommitResult(boolean status, Long reservationId, Instant expireAt) implements Serializable {
}
