package com.asrevo.cvhome.checkout.model.order;

/**
 * The vocabulary of the order ledger.
 */
public enum OrderEventType {
    PLACED,
    RESERVED,
    RESERVATION_REFUSED,
    PAYMENT_INITIATED,
    PAYMENT_INITIATE_REJECTED,
    PAYMENT_SIGNAL,
    PAYMENT_AFTER_CLOSE,
    RESERVATION_EXPIRED,
    COMMITTED,
    COMMIT_REFUSED,
    RELEASED,
    EXPIRED,
    CANCELLED,
    STATUS_CHANGED,
    RECOVERY_RETRIED,
    RECOVERY_GAVE_UP
}
