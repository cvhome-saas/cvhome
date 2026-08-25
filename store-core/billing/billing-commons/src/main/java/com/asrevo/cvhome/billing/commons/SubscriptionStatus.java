package com.asrevo.cvhome.billing.commons;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Lifecycle of one store's subscription, and the only place the legal transitions are written down.
 *
 * <p>
 * The literals are also the {@code CHECK} constraint on {@code billing.store_subscription.status} (and on both status
 * columns of {@code billing.subscription_audit}), so adding a state means a DDL edit in the same change.
 * </p>
 */
public enum SubscriptionStatus {

    /**
     * A row exists but no plan was ever paid for — the state a store lands in when its org has already spent its one
     * trial. The store is created and visible to its owner, it just cannot be worked in until someone pays.
     */
    PENDING,

    /** The org's single 14-day trial, granted to whichever store it creates first. No card on file yet. */
    TRIALING,

    /** Paid, with an open billing period. */
    ACTIVE,

    /**
     * A renewal invoice failed and the grace window is running. Deliberately still operable: a merchant who cannot
     * take orders cannot earn the money to pay the invoice.
     */
    PAST_DUE,

    /** The grace window expired, or a trial ended without payment. Seller access is gated; the storefront stays up. */
    SUSPENDED,

    /** Terminal for this Stripe subscription. Subscribing again returns the row to {@link #PENDING}. */
    CANCELED;

    private static final Map<SubscriptionStatus, Set<SubscriptionStatus>> LEGAL = Map.of(
            PENDING, EnumSet.of(TRIALING, ACTIVE, CANCELED),
            TRIALING, EnumSet.of(ACTIVE, SUSPENDED, CANCELED),
            ACTIVE, EnumSet.of(PAST_DUE, SUSPENDED, CANCELED),
            PAST_DUE, EnumSet.of(ACTIVE, SUSPENDED, CANCELED),
            SUSPENDED, EnumSet.of(ACTIVE, CANCELED),
            CANCELED, EnumSet.of(PENDING));

    /**
     * Whether this state may move to {@code target}.
     *
     * <p>
     * Staying put is deliberately legal. Stripe redelivers webhooks, and the same event arriving twice must be a
     * no-op rather than a failure — this is the backstop behind {@code billing.processed_stripe_event}, not a
     * substitute for it.
     * </p>
     */
    public boolean canTransitionTo(SubscriptionStatus target) {
        return this == target || LEGAL.getOrDefault(this, Set.of()).contains(target);
    }

    /**
     * Whether the store may be worked in — the single question every enforcement layer asks.
     *
     * <p>
     * {@link #PAST_DUE} counts as operable on purpose: it is a warning window, not a shutdown.
     * </p>
     */
    public boolean operable() {
        return this == TRIALING || this == ACTIVE || this == PAST_DUE;
    }

}
