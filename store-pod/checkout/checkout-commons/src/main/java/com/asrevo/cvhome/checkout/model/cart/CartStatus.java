package com.asrevo.cvhome.checkout.model.cart;

/**
 * {@code ACTIVE} carts take edits; a {@code CONVERTED} cart is the frozen source of an order and is read-only while
 * that order is open, and gone (404) once the order closes.
 */
public enum CartStatus {
    ACTIVE, CONVERTED
}
