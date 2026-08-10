package com.asrevo.cvhome.billing.service.stripe;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.billing.commons.ProcessedEventOutcome;
import com.asrevo.cvhome.billing.commons.StripeCustomerId;
import com.asrevo.cvhome.billing.commons.StripeSubscriptionId;
import com.asrevo.cvhome.billing.domain.StoreSubscriptionEntity;
import com.asrevo.cvhome.billing.events.stripe.StripeWebhookReceivedEvent;
import com.asrevo.cvhome.billing.repository.ProcessedStripeEventRepository;
import com.asrevo.cvhome.billing.repository.StoreSubscriptionRepository;
import com.google.gson.JsonObject;
import com.stripe.model.Event;

import io.namastack.outbox.Outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Accepts a verified Stripe event exactly once, and hands the work to the outbox.
 *
 * <p>
 * Deliberately tiny. Everything it does has to fit in one short transaction, because it runs on the HTTP thread while
 * Stripe waits — and because the shorter it is, the smaller the window in which a crash could lose an event.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookIngestService {

    /**
     * The event types that change something here. Anything else is recorded and dropped: Stripe sends dozens of
     * types, and acting on an unrecognised one is worse than ignoring it.
     */
    private static final Set<String> HANDLED_TYPES = Set.of(
            "checkout.session.completed",
            "customer.subscription.created",
            "customer.subscription.updated",
            "customer.subscription.deleted",
            "customer.subscription.paused",
            "invoice.payment_succeeded",
            "invoice.payment_failed");

    private static final String STORE_METADATA_KEY = "storeId";

    private final ProcessedStripeEventRepository processedRepository;

    private final StoreSubscriptionRepository subscriptionRepository;

    private final Outbox outbox;

    /**
     * Records the event and schedules the work.
     *
     * <p>
     * The claim is the whole idempotency story. Two deliveries of one event race on the primary key; one wins and
     * enqueues, the other sees zero rows affected and returns. Because the insert and the {@code outbox.schedule}
     * share a transaction, an event is never recorded without its work also being queued — the failure mode that
     * would silently drop a payment.
     * </p>
     */
    @Transactional
    public void ingest(Event event) {
        String rawJson = event.getDataObjectDeserializer().getRawJson();
        JsonObject data = StripeJson.parse(rawJson);
        Optional<String> store = storeOf(event.getType(), data);
        boolean handled = HANDLED_TYPES.contains(event.getType()) && store.isPresent();
        ProcessedEventOutcome outcome = handled ? ProcessedEventOutcome.SCHEDULED : ProcessedEventOutcome.IGNORED;
        Instant now = Instant.now();

        int claimed = processedRepository.claim(event.getId(), event.getType(), event.getApiVersion(),
                outcome.name(), now, handled ? null : now);
        if (claimed == 0) {
            log.info("Stripe event {} of type {} was already accepted — ignoring the redelivery", event.getId(),
                    event.getType());
            return;
        }
        if (!handled) {
            log.debug("Stripe event {} of type {} needs no action here", event.getId(), event.getType());
            return;
        }
        outbox.schedule(StripeWebhookReceivedEvent.of(event.getId(), event.getType(), store.get(), rawJson),
                store.get());
        log.info("Accepted Stripe event {} of type {} for store {}", event.getId(), event.getType(), store.get());
    }

    /**
     * Which store an event is about.
     *
     * <p>
     * Read from the raw JSON rather than the SDK's model objects, and for a concrete reason: {@code Invoice} exposed
     * {@code getSubscription()} until Stripe moved it under {@code parent.subscription_details}, so a build that
     * compiled against one library version stops resolving invoices on another. The document has both shapes, so
     * looking there works across versions.
     * </p>
     *
     * <p>
     * Three routes, because Stripe attributes objects differently: a checkout session carries the store as its client
     * reference, a subscription carries it in metadata we set at checkout, and an invoice carries neither — it names
     * the subscription, which we map back ourselves. An event we cannot attribute is recorded as ignored rather than
     * failed: it is usually about an object created outside this service, and a non-2xx would have Stripe redeliver
     * it for days.
     * </p>
     */
    private Optional<String> storeOf(String eventType, JsonObject data) {
        if (eventType.startsWith("checkout.session.")) {
            return Optional.ofNullable(StripeJson.string(data, StripeFields.CLIENT_REFERENCE_ID));
        }
        if (eventType.startsWith("customer.subscription.")) {
            return Optional.ofNullable(StripeJson.string(StripeJson.object(data, StripeFields.METADATA), STORE_METADATA_KEY));
        }
        if (eventType.startsWith("invoice.")) {
            return storeOfInvoice(eventType, data);
        }
        return Optional.empty();
    }

    /**
     * The store an invoice belongs to, by way of the subscription it names.
     *
     * <p>
     * When the subscription is not bound yet but the invoice's customer is one of ours, this refuses to answer at all
     * rather than answering "unknown". Stripe does not order deliveries, so the invoice can genuinely arrive before
     * the subscription event that binds it — and calling it unattributable would record the event id and drop a paid
     * invoice from history for good. Failing makes Stripe redeliver once the binding exists.
     * </p>
     */
    private Optional<String> storeOfInvoice(String eventType, JsonObject invoice) {
        Optional<String> store = subscriptionOfInvoice(invoice)
                .map(StripeSubscriptionId::new)
                .flatMap(subscriptionRepository::findByStripeSubscriptionId)
                .map(StoreSubscriptionEntity::getId)
                .map(it -> it.getId().toString());
        if (store.isPresent() || !HANDLED_TYPES.contains(eventType)) {
            return store;
        }
        String customer = StripeJson.string(invoice, StripeFields.CUSTOMER);
        boolean ours = customer != null
                && subscriptionRepository.existsByStripeCustomerId(new StripeCustomerId(customer));
        if (ours) {
            throw new WebhookNotYetAttributableException(StripeJson.string(invoice, StripeFields.ID), eventType);
        }
        return Optional.empty();
    }

    /**
     * The subscription an invoice belongs to, in either of the shapes Stripe has used for it.
     */
    private Optional<String> subscriptionOfInvoice(JsonObject invoice) {
        String direct = StripeJson.string(invoice, StripeFields.SUBSCRIPTION);
        if (direct != null) {
            return Optional.of(direct);
        }
        JsonObject details = StripeJson.object(StripeJson.object(invoice, StripeFields.PARENT), StripeFields.SUBSCRIPTION_DETAILS);
        String nested = StripeJson.string(details, StripeFields.SUBSCRIPTION);
        if (nested != null) {
            return Optional.of(nested);
        }
        return Optional.ofNullable(StripeJson.string(StripeJson.firstOfData(invoice, StripeFields.LINES), StripeFields.SUBSCRIPTION));
    }

}
