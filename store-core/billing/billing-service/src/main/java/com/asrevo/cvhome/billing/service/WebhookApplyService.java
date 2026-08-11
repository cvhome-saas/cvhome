package com.asrevo.cvhome.billing.service;

import com.asrevo.cvhome.billing.commons.errors.IllegalSubscriptionTransitionException;
import com.asrevo.cvhome.billing.commons.errors.PlanPriceNotFoundException;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionNotFoundException;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;

/**
 * Applies what Stripe reported to the local subscription.
 *
 * <p>
 * Every method here is idempotent, because every one of them can be called twice: Stripe redelivers, and the outbox
 * retries. The inbound event table stops most duplicates, but correctness cannot rest on it alone — an event that was
 * accepted and then failed mid-handling will be retried from the outbox with the same content.
 * </p>
 */
public interface WebhookApplyService {

    /**
     * Binds the Stripe customer and subscription a completed checkout produced.
     *
     * <p>
     * Records identifiers only. The subscription does not become active here: a completed checkout means the customer
     * finished the form, and it is the invoice event that says money actually moved.
     * </p>
     */
    void applyCheckoutCompleted(ManagerStoreId store, String eventId, String payload)
            throws SubscriptionNotFoundException;

    /**
     * Reconciles the local subscription with Stripe's view of it — status, period window and current plan.
     *
     * @throws PlanPriceNotFoundException Stripe named a price this catalog does not know
     */
    void applySubscriptionChanged(ManagerStoreId store, String eventId, String payload)
            throws SubscriptionNotFoundException, IllegalSubscriptionTransitionException, PlanPriceNotFoundException;

    /**
     * Ends the subscription because Stripe ended it.
     */
    void applySubscriptionEnded(ManagerStoreId store, String eventId, String payload)
            throws SubscriptionNotFoundException, IllegalSubscriptionTransitionException;

    /**
     * Records a paid invoice and opens the period it paid for.
     */
    void applyInvoicePaid(ManagerStoreId store, String eventId, String payload)
            throws SubscriptionNotFoundException, IllegalSubscriptionTransitionException, PlanPriceNotFoundException;

    /**
     * Records a failed invoice and starts the grace window.
     */
    void applyInvoiceFailed(ManagerStoreId store, String eventId, String payload)
            throws SubscriptionNotFoundException, IllegalSubscriptionTransitionException;

}
