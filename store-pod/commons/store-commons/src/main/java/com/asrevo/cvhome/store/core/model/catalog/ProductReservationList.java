package com.asrevo.cvhome.store.core.model.catalog;

import java.time.Instant;
import java.util.Set;

/**
 * What checkout asks inventory to hold for an order. {@code expireAt} is how long the caller wants the hold to last
 * — a manual bank transfer needs days where a card payment needs minutes; {@code null} leaves it to inventory's
 * default, and inventory caps it either way.
 */
public record ProductReservationList(Set<ReserveProductEntry> entries, Instant expireAt) {

    public ProductReservationList(Set<ReserveProductEntry> entries) {
        this(entries, null);
    }
}
