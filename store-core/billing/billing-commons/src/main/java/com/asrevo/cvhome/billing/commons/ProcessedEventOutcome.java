package com.asrevo.cvhome.billing.commons;

/**
 * What became of a Stripe event we accepted. The literals are the {@code CHECK} constraint on
 * {@code billing.processed_stripe_event.outcome}.
 */
public enum ProcessedEventOutcome {

    /** Handed to the outbox; the work happens after commit. */
    SCHEDULED,

    /** Recognised and deliberately not acted on — an event type we do not handle, or one we cannot attribute to a store. */
    IGNORED,

    /** Handling failed after the outbox exhausted its retries. Left for an operator to look at. */
    FAILED

}
