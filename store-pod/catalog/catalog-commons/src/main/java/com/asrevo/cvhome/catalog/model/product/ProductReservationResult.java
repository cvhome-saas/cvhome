package com.asrevo.cvhome.catalog.model.product;

import java.io.Serializable;
import java.time.Instant;

public record ProductReservationResult(boolean status, Long reservationId, Instant expireAt) implements Serializable {
    public ProductReservationResult(boolean status) {
        this(status, null, null);
    }
}
