package com.asrevo.cvhome.checkout.model.order;

/**
 * Who caused an order event. Together with {@code sourceRef} it is the dedup key of inbound signals.
 */
public enum OrderEventSource {
    PLACEMENT, PAYMENT, INVENTORY, CONSOLE, JOB, SYSTEM
}
