package com.asrevo.cvhome.billing.commons;

/**
 * Status of a subscription invoice, mirroring Stripe's invoice states. The literals are the {@code CHECK} constraint
 * on {@code billing.subscription_invoice.status}.
 */
public enum InvoiceStatus {

    /** Not yet finalized by Stripe; amounts may still change. */
    DRAFT,

    /** Finalized and awaiting payment. */
    OPEN,

    /** Settled. */
    PAID,

    /** Written off — Stripe gave up collecting. */
    UNCOLLECTIBLE,

    /** Cancelled before payment; owes nothing. */
    VOID

}
