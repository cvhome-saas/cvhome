package com.asrevo.cvhome.billing.service.stripe;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.asrevo.cvhome.billing.commons.ProcessedEventOutcome;
import com.asrevo.cvhome.billing.commons.StripeCustomerId;
import com.asrevo.cvhome.billing.commons.StripeSubscriptionId;
import com.asrevo.cvhome.billing.domain.StoreSubscriptionEntity;
import com.asrevo.cvhome.billing.events.stripe.StripeWebhookReceivedEvent;
import com.asrevo.cvhome.billing.repository.ProcessedStripeEventRepository;
import com.asrevo.cvhome.billing.repository.StoreSubscriptionRepository;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import io.namastack.outbox.Outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Accepting a verified Stripe event exactly once, and working out which store it is about.
 *
 * <p>
 * Every case runs a real fixture through the real verifier, so what is exercised is the payload shape Stripe
 * actually delivers. Attribution is the hard part: a checkout session carries the store as its client reference, a
 * subscription carries it in metadata set at checkout, and an invoice carries neither — it names the subscription,
 * which has to be mapped back locally, in either of the two shapes Stripe has used for that field.
 * </p>
 */
class WebhookIngestServiceTest {

    private static final String STORE = "65f023632bc46470c104b76f";

    private ProcessedStripeEventRepository processed;

    private StoreSubscriptionRepository subscriptions;

    private Outbox outbox;

    private WebhookIngestService service;

    @BeforeEach
    void setUp() {
        processed = mock(ProcessedStripeEventRepository.class);
        subscriptions = mock(StoreSubscriptionRepository.class);
        outbox = mock(Outbox.class);
        // The common case: this delivery is the first, so the claim succeeds.
        when(processed.claim(anyString(), anyString(), any(), anyString(), any(), any())).thenReturn(1);
        service = new WebhookIngestService(processed, subscriptions, outbox);
    }

    private static StoreSubscriptionEntity boundSubscription() {
        StoreSubscriptionEntity entity = StoreSubscriptionEntity.pending(new StoreMerchantId(STORE),
                new ManagerOrgId("32a034a43cd77581d105c87a"));
        return entity.bindProvider(new StripeCustomerId("cus_test_1"), new StripeSubscriptionId("sub_test_1"));
    }

    private void subscriptionIsBound() {
        when(subscriptions.findByStripeSubscriptionId(new StripeSubscriptionId("sub_test_1")))
                .thenReturn(Optional.of(boundSubscription()));
    }

    private StripeWebhookReceivedEvent scheduled() {
        ArgumentCaptor<StripeWebhookReceivedEvent> event =
                ArgumentCaptor.forClass(StripeWebhookReceivedEvent.class);
        verify(outbox).schedule(event.capture(), anyString());
        return event.getValue();
    }

    // --------------------------------------------------------------------------------------------- attribution

    @Test
    @DisplayName("a checkout session is attributed by its client reference")
    void checkoutSessionIsAttributedByClientReference() {
        service.ingest(StripeEventFixtures.event("checkout-session-completed.json"));

        StripeWebhookReceivedEvent event = scheduled();
        assertThat(event.store()).isEqualTo(STORE);
        assertThat(event.stripeEventType()).isEqualTo("checkout.session.completed");
        assertThat(event.eventId()).isEqualTo("evt_checkout_completed");
        // The payload carried through is the data object, not the envelope — the apply handlers read the object.
        assertThat(event.payload()).contains("cs_test_1");
    }

    @Test
    @DisplayName("a subscription event is attributed by the metadata set at checkout")
    void subscriptionIsAttributedByMetadata() {
        service.ingest(StripeEventFixtures.event("subscription-updated.json"));

        assertThat(scheduled().store()).isEqualTo(STORE);
    }

    @Test
    @DisplayName("an invoice is attributed through the subscription it names, in the 2025 shape")
    void invoiceIsAttributedThroughItsSubscription() {
        subscriptionIsBound();

        service.ingest(StripeEventFixtures.event("invoice-payment-succeeded.json"));

        // An invoice carries no store at all. It names its subscription under parent.subscription_details, which is
        // where Stripe moved the field, and we map that back ourselves.
        assertThat(scheduled().store()).isEqualTo(STORE);
    }

    @Test
    @DisplayName("an invoice in the pre-2025 shape, with the subscription at the top level, still resolves")
    void invoiceInTheLegacyShapeResolves() {
        subscriptionIsBound();

        service.ingest(StripeEventFixtures.event("invoice-payment-succeeded-legacy.json"));

        // The reason the raw JSON is read rather than the SDK's Invoice model: a build compiled against one library
        // version has to keep resolving invoices delivered in the other.
        assertThat(scheduled().store()).isEqualTo(STORE);
    }

    @Test
    @DisplayName("a failed invoice is attributed the same way")
    void failedInvoiceIsAttributed() {
        subscriptionIsBound();

        service.ingest(StripeEventFixtures.event("invoice-payment-failed.json"));

        assertThat(scheduled().store()).isEqualTo(STORE);
    }

    // ------------------------------------------------------------------------------------------------ ignoring

    @Test
    @DisplayName("an event type nothing here acts on is recorded and dropped")
    void anUnhandledTypeIsRecordedAndIgnored() {
        service.ingest(StripeEventFixtures.event("customer-updated.json"));

        // Recorded, so a redelivery is cheap; not scheduled, because acting on an unrecognised type is worse than
        // ignoring it. Stripe sends dozens.
        verify(processed).claim(eq("evt_customer_updated"), eq("customer.updated"), any(),
                eq(ProcessedEventOutcome.IGNORED.name()), any(Instant.class), any(Instant.class));
        verify(outbox, never()).schedule(any(), anyString());
    }

    @Test
    @DisplayName("a handled type with no store on it is recorded as ignored rather than failed")
    void aHandledTypeWithoutAStoreIsIgnored() {
        service.ingest(StripeEventFixtures.event("subscription-updated-no-store.json"));

        // Usually an object created outside this service. A non-2xx would have Stripe redeliver it for days.
        verify(processed).claim(eq("evt_subscription_no_store"), anyString(), any(),
                eq(ProcessedEventOutcome.IGNORED.name()), any(Instant.class), any(Instant.class));
        verify(outbox, never()).schedule(any(), anyString());
    }

    @Test
    @DisplayName("a checkout session with no client reference is ignored")
    void anUnattributedCheckoutSessionIsIgnored() {
        service.ingest(StripeEventFixtures.event("checkout-session-completed-unattributed.json"));

        verify(processed).claim(anyString(), anyString(), any(), eq(ProcessedEventOutcome.IGNORED.name()),
                any(Instant.class), any(Instant.class));
        verify(outbox, never()).schedule(any(), anyString());
    }

    @Test
    @DisplayName("an invoice for a customer that was never ours is ignored, not retried")
    void aForeignInvoiceIsIgnored() {
        when(subscriptions.existsByStripeCustomerId(new StripeCustomerId("cus_someone_else"))).thenReturn(false);

        service.ingest(StripeEventFixtures.event("invoice-created-unattributable.json"));

        verify(outbox, never()).schedule(any(), anyString());
    }

    // ------------------------------------------------------------------------------------- ordering and retries

    @Test
    @DisplayName("an invoice for one of our customers whose subscription is not bound yet fails, so Stripe retries")
    void anInvoiceThatArrivedTooEarlyFails() {
        when(subscriptions.findByStripeSubscriptionId(new StripeSubscriptionId("sub_not_bound_yet")))
                .thenReturn(Optional.empty());
        when(subscriptions.existsByStripeCustomerId(new StripeCustomerId("cus_test_1"))).thenReturn(true);

        // Stripe does not order deliveries, so a paid invoice can genuinely arrive before the subscription event
        // that binds it. Recording it as ignored would drop a payment from history for good, because the event id
        // would then be known and never reprocessed. Failing makes Stripe redeliver once the binding exists.
        assertThatThrownBy(() -> service.ingest(
                StripeEventFixtures.event("invoice-payment-succeeded-unbound.json")))
                .isInstanceOf(WebhookNotYetAttributableException.class)
                .hasMessageContaining("in_test_unbound");

        verify(processed, never()).claim(anyString(), anyString(), any(), anyString(), any(), any());
        verify(outbox, never()).schedule(any(), anyString());
    }

    @Test
    @DisplayName("a redelivery of an event already accepted schedules nothing")
    void aRedeliveryIsDropped() {
        when(processed.claim(anyString(), anyString(), any(), anyString(), any(), any())).thenReturn(0);

        service.ingest(StripeEventFixtures.event("subscription-updated.json"));

        // The claim is the whole idempotency story: two deliveries race on the primary key, one wins and enqueues,
        // the other sees zero rows and returns.
        verify(outbox, never()).schedule(any(), anyString());
    }

    @Test
    @DisplayName("a scheduled event is recorded as SCHEDULED with no processed time")
    void aScheduledEventIsNotYetProcessed() {
        service.ingest(StripeEventFixtures.event("subscription-updated.json"));

        ArgumentCaptor<Instant> processedAt = ArgumentCaptor.forClass(Instant.class);
        verify(processed).claim(eq("evt_subscription_updated"), eq("customer.subscription.updated"), any(),
                eq(ProcessedEventOutcome.SCHEDULED.name()), any(Instant.class), processedAt.capture());
        // Null, because the work has only been queued. A processed_at written here would make a crash before the
        // handler ran indistinguishable from a completed one.
        assertThat(processedAt.getValue()).isNull();
    }

    @Test
    @DisplayName("the outbox is partitioned by store, which is what stands in for a distributed lock")
    void schedulesOnTheStorePartition() {
        service.ingest(StripeEventFixtures.event("subscription-updated.json"));

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        verify(outbox).schedule(any(StripeWebhookReceivedEvent.class), key.capture());
        assertThat(key.getValue()).isEqualTo(STORE);
    }

    @Test
    @DisplayName("the API version Stripe sent is recorded, because a payload's shape depends on it")
    void recordsTheApiVersion() {
        service.ingest(StripeEventFixtures.event("subscription-updated-items-period.json"));

        verify(processed).claim(anyString(), anyString(), eq("2025-06-30.basil"), anyString(), any(), any());
    }

    @Test
    @DisplayName("a deleted subscription is scheduled like any other handled type")
    void deletionIsHandled() {
        service.ingest(StripeEventFixtures.event("subscription-deleted.json"));

        assertThat(scheduled().stripeEventType()).isEqualTo("customer.subscription.deleted");
    }

}
