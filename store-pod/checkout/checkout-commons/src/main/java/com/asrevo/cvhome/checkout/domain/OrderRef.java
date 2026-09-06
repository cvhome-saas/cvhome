package com.asrevo.cvhome.checkout.domain;

import java.util.UUID;

/**
 * The opaque reference an order is known by outside this service — the {@code ref} inventory keys a reservation on
 * and payment keys a transaction on. A UUID, so it cannot be guessed from the numeric id and cannot collide across
 * stores.
 */
public record OrderRef(String value) {

    public OrderRef {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("order ref must not be blank");
        }
    }

    public static OrderRef newRef() {
        return new OrderRef(UUID.randomUUID().toString());
    }

    public static OrderRef of(String value) {
        return new OrderRef(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
