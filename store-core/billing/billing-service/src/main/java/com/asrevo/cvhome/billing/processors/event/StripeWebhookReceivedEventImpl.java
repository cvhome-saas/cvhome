package com.asrevo.cvhome.billing.processors.event;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.billing.commons.errors.IllegalSubscriptionTransitionException;
import com.asrevo.cvhome.billing.commons.errors.PlanPriceNotFoundException;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionNotFoundException;
import com.asrevo.cvhome.billing.events.stripe.StripeWebhookReceivedEvent;
import com.asrevo.cvhome.billing.service.WebhookApplyService;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.errors.UncheckedBaseException;

import io.namastack.outbox.annotation.OutboxHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Does the work a Stripe event asked for, after the webhook request has already been answered.
 *
 * <p>
 * Off the HTTP thread on purpose: Stripe's delivery has a timeout, and a slow handler holding it open turns into a
 * redelivery and duplicated work. Here a failure costs a retry instead.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StripeWebhookReceivedEventImpl implements EventImpl<StripeWebhookReceivedEvent> {

    private final WebhookApplyService applyService;

    /**
     * Routes the event, and decides what a failure means.
     *
     * <p>
     * The two exception branches are the point. A subscription that has vanished, or a transition that is no longer
     * legal, will fail identically on every retry — that is a fact about our data, so it is logged and the record is
     * allowed to complete rather than burning the outbox's attempts. A price we do not recognise, or anything else,
     * is rethrown so the outbox retries: an unpublished price usually means the catalog sync has not run yet, and
     * that resolves on its own.
     * </p>
     */
    @Override
    @OutboxHandler
    public void process(StripeWebhookReceivedEvent event) {
        ManagerStoreId store = new ManagerStoreId(event.store());
        log.info("Applying Stripe event {} of type {} to store {}", event.eventId(), event.stripeEventType(),
                store);
        try {
            dispatch(event, store);
        } catch (SubscriptionNotFoundException | IllegalSubscriptionTransitionException e) {
            log.warn("Stripe event {} cannot be applied to store {} and will not become applicable: {}",
                    event.eventId(), store, e.getMessage());
        } catch (PlanPriceNotFoundException e) {
            // Retryable: normally the catalog has not been published to Stripe yet.
            throw new UncheckedBaseException(e);
        }
    }

    private void dispatch(StripeWebhookReceivedEvent event, ManagerStoreId store)
            throws SubscriptionNotFoundException, IllegalSubscriptionTransitionException, PlanPriceNotFoundException {
        switch (event.stripeEventType()) {
            case "checkout.session.completed" ->
                    applyService.applyCheckoutCompleted(store, event.eventId(), event.payload());
            case "customer.subscription.created", "customer.subscription.updated" ->
                    applyService.applySubscriptionChanged(store, event.eventId(), event.payload());
            case "customer.subscription.deleted", "customer.subscription.paused" ->
                    applyService.applySubscriptionEnded(store, event.eventId(), event.payload());
            case "invoice.payment_succeeded" ->
                    applyService.applyInvoicePaid(store, event.eventId(), event.payload());
            case "invoice.payment_failed" ->
                    applyService.applyInvoiceFailed(store, event.eventId(), event.payload());
            default -> log.info("No handler for Stripe event type {} — ingest should not have scheduled it",
                    event.stripeEventType());
        }
    }

}
