package com.asrevo.cvhome.billing.commons;

/**
 * Who drove a subscription change. Recorded on every audit row, because "the plan changed" is only answerable as a
 * support question once you know whether a person, Stripe, or one of our own jobs did it.
 */
public enum ChangeSource {

    /** A seller or admin acting through the REST API. */
    API,

    /** A Stripe webhook. */
    WEBHOOK,

    /** One of the scheduled jobs (trial expiry, suspension, deferred plan change). */
    JOB,

    /** Automatic provisioning — the row being created alongside its store. */
    SYSTEM

}
