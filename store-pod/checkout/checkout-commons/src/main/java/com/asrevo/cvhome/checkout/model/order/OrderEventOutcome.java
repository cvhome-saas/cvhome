package com.asrevo.cvhome.checkout.model.order;

/**
 * What an event did to the order. {@code DUPLICATE} and {@code IGNORED} are recorded on purpose: a signal that
 * changed nothing is still evidence the signal arrived.
 */
public enum OrderEventOutcome {
    APPLIED, DUPLICATE, IGNORED
}
