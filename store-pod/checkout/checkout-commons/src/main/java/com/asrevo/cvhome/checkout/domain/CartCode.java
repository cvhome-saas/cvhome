package com.asrevo.cvhome.checkout.domain;

import java.util.UUID;

/**
 * The code a shopper's browser holds for its cart. Anonymous by design: the storefront keeps it in localStorage and
 * a shopper identity only enters at checkout.
 */
public record CartCode(String value) {

    public CartCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("cart code must not be blank");
        }
    }

    public static CartCode newCode() {
        return new CartCode(UUID.randomUUID().toString());
    }

    public static CartCode of(String value) {
        return new CartCode(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
