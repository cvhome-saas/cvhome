package com.asrevo.cvhome.inventory.entity;

/**
 * Lifecycle of a reservation: held until expiry, then either kept for the order or given back.
 */
public enum ProductReservationStatus {
    TEMPORARY_RESERVED, COMPLETED, ROLLBACK
}
