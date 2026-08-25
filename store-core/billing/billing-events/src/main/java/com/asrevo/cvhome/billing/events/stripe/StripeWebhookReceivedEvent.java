package com.asrevo.cvhome.billing.events.stripe;

import java.util.Map;

import com.asrevo.cvhome.commons.event.Event;

import io.namastack.outbox.annotation.OutboxEvent;

/**
 * A verified Stripe event, handed to the outbox so the work happens after the HTTP request has been answered.
 *
 * <p>
 * The webhook endpoint does almost nothing itself: verify the signature, record the event id, enqueue this. Doing the
 * work inline would mean Stripe's delivery timeout decides how long our database transaction may take, and a slow
 * handler would turn into redelivered events and duplicated work.
 * </p>
 *
 * <p>
 * Partitioned on the store, so one store's events are handled in the order Stripe sent them — an
 * {@code invoice.payment_failed} overtaking the {@code invoice.payment_succeeded} that followed it would leave a
 * paying store suspended. Ordering across stores is not promised and is not needed.
 * </p>
 *
 * <p>
 * The Stripe type is called {@code stripeEventType}, not {@code eventType}, and the name is load-bearing:
 * {@link Event#eventType()} is this envelope's own type, and a record component of that name would be shadowed by the
 * override below. That collision silently fed the envelope's class name into the handler's switch, which then matched
 * nothing and dropped every payment event.
 * </p>
 *
 * @param eventId         Stripe's event id, the key the inbound idempotency table is built on
 * @param stripeEventType Stripe's event type, e.g. {@code invoice.payment_succeeded}
 * @param store           the store this event concerns, already resolved at ingest
 * @param payload         the raw JSON of the event's data object, kept verbatim so a handler reads what Stripe
 *                        actually sent rather than something re-serialised through our own types
 * @param data            unused, present because {@link Event} requires it
 */
@OutboxEvent(key = "#this.store()")
public record StripeWebhookReceivedEvent(String eventId, String stripeEventType, String store, String payload,
                                         Map<String, String> data) implements Event {

    public static StripeWebhookReceivedEvent of(String eventId, String stripeEventType, String store,
                                                String payload) {
        return new StripeWebhookReceivedEvent(eventId, stripeEventType, store, payload, Map.of());
    }

    @Override
    public String eventType() {
        return StripeWebhookReceivedEvent.class.getSimpleName();
    }

}
