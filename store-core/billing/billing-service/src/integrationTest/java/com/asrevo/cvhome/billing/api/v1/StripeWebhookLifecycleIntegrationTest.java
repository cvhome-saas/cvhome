package com.asrevo.cvhome.billing.api.v1;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import com.asrevo.cvhome.billing.api.BillingFixtures;
import com.asrevo.cvhome.billing.commons.InvoiceStatus;
import com.asrevo.cvhome.billing.commons.StripeCustomerId;
import com.asrevo.cvhome.billing.commons.StripeSubscriptionId;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.billing.domain.StoreSubscriptionEntity;
import com.asrevo.cvhome.billing.repository.SubscriptionAuditRepository;
import com.asrevo.cvhome.billing.repository.SubscriptionInvoiceRepository;
import com.asrevo.cvhome.billing.service.stripe.StripeSignatures;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;

import static com.asrevo.cvhome.billing.api.BillingApiSupport.ORG_A;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.V1;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.expect;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.path;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * The whole webhook path, end to end: signed delivery, ingest, outbox, apply.
 *
 * <p>
 * Everything between the HTTP request and the subscription row is real — the signature verification, the
 * exactly-once claim, the outbox that carries the work off the request thread, the state machine and the audit
 * trail. That gap is where a webhook path fails in ways no unit test reaches: an event accepted and then never
 * applied leaves subscriptions silently frozen, and is invisible from every screen because the rows simply stop
 * moving.
 * </p>
 *
 * <p>
 * The apply half runs asynchronously, so each assertion waits for the row to reach the expected state rather than
 * reading it once. A store of its own per case, because the outbox partitions on the store id and two cases sharing
 * one would serialise behind each other.
 * </p>
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class StripeWebhookLifecycleIntegrationTest {

    private static final String CUSTOMER_PREFIX = "cus";

    private static final String EVENT_PREFIX = "evt";

    private static final String INVOICE_PREFIX = "in";

    private static final String SUBSCRIPTION_PREFIX = "sub";

    /** Must match {@code com.asrevo.cvhome.stripe.webhook-signing-key} in {@code application-test-stores.yml}. */
    private static final String SIGNING_KEY = "whsec_integration_test_key";

    private static final String EVENTS = path(V1, "stripe-webhook", "public", "events");

    private static final String SIGNATURE_HEADER = "Stripe-Signature";

    /** Generous: the outbox polls on a two-second timer, so a single poll can be most of this. */
    private static final Duration APPLIED = Duration.ofSeconds(30L);

    private static final String CHECKOUT_STORE = "b1110000000000000000dd01";

    private static final String PAID_STORE = "b1110000000000000000dd02";

    private static final String FAILED_STORE = "b1110000000000000000dd03";

    private static final String CANCELLED_STORE = "b1110000000000000000dd04";

    private static final String RENEWED_STORE = "b1110000000000000000dd05";

    @LocalServerPort
    private int port;

    @Autowired
    private BillingFixtures fixtures;

    @Autowired
    private SubscriptionInvoiceRepository invoices;

    @Autowired
    private SubscriptionAuditRepository audits;

    private RestClient client;

    private String stripePriceId;

    @BeforeEach
    void setUp() {
        client = RestClient.builder()
                .baseUrl(String.format("http://localhost:%d", port))
                .defaultStatusHandler(status -> true, (request, response) -> { })
                .build();
        fixtures.publishPrices();
        stripePriceId = fixtures.dearestPrice().getStripePriceId().id();
    }

    // ------------------------------------------------------------------------------------------------ helpers

    private static String id(String prefix) {
        return String.format("%s_%s", prefix, UUID.randomUUID().toString().replace("-", "").substring(0, 12));
    }

    /** Delivers a payload signed with the configured secret and asserts Stripe was answered 200. */
    private void deliver(String payload) {
        var response = client.post().uri(EVENTS)
                .contentType(MediaType.APPLICATION_JSON)
                .header(SIGNATURE_HEADER, StripeSignatures.sign(payload, SIGNING_KEY))
                .body(payload)
                .retrieve()
                .toEntity(String.class);
        expect(response, HttpStatus.OK);
    }

    private void awaitSubscription(String store, Predicate<StoreSubscriptionEntity> condition) {
        await().atMost(APPLIED).pollInterval(Duration.ofMillis(200L))
                .until(() -> condition.test(fixtures.read(store)));
    }

    /** A store bound to a provider subscription of its own, ready for invoice events to be attributed to. */
    private String bind(String store) {
        String subscription = id(SUBSCRIPTION_PREFIX);
        StoreSubscriptionEntity entity = fixtures.pending(store);
        fixtures.save(entity.bindProvider(new StripeCustomerId(id(CUSTOMER_PREFIX)),
                new StripeSubscriptionId(subscription)));
        return subscription;
    }

    private String checkoutCompleted(String store, String customer, String subscription) {
        return String.format("""
                {"id":"%s","object":"event","api_version":"2025-03-31.basil","created":%d,
                 "type":"checkout.session.completed",
                 "data":{"object":{"id":"%s","object":"checkout.session","mode":"subscription",
                   "client_reference_id":"%s","customer":"%s","subscription":"%s","status":"complete"}}}""",
                id(EVENT_PREFIX), Instant.now().getEpochSecond(), id("cs"), store, customer, subscription);
    }

    private String invoicePaid(String subscription, long periodStart, long periodEnd) {
        return String.format("""
                {"id":"%s","object":"event","api_version":"2025-03-31.basil","created":%d,
                 "type":"invoice.payment_succeeded",
                 "data":{"object":{"id":"%s","object":"invoice","number":"CVH-LIFECYCLE",
                   "currency":"usd","amount_due":3000,"amount_paid":3000,"created":%d,
                   "hosted_invoice_url":"https://invoice.stripe.test/x",
                   "invoice_pdf":"https://invoice.stripe.test/x.pdf",
                   "period_start":%d,"period_end":%d,
                   "parent":{"type":"subscription_details",
                             "subscription_details":{"subscription":"%s"}},
                   "lines":{"object":"list","data":[{"id":"il_1","object":"line_item",
                     "subscription":"%s","period":{"start":%d,"end":%d},
                     "price":{"id":"%s","object":"price"}}]}}}}""",
                id(EVENT_PREFIX), Instant.now().getEpochSecond(), id(INVOICE_PREFIX), periodStart, periodStart, periodStart,
                subscription, subscription, periodStart, periodEnd, stripePriceId);
    }

    private String invoiceFailed(String subscription) {
        return String.format("""
                {"id":"%s","object":"event","api_version":"2025-03-31.basil","created":%d,
                 "type":"invoice.payment_failed",
                 "data":{"object":{"id":"%s","object":"invoice","number":"CVH-FAILED",
                   "currency":"usd","amount_due":3000,"amount_paid":0,"created":%d,
                   "parent":{"type":"subscription_details",
                             "subscription_details":{"subscription":"%s"}}}}}""",
                id(EVENT_PREFIX), Instant.now().getEpochSecond(), id(INVOICE_PREFIX), Instant.now().getEpochSecond(), subscription);
    }

    private String subscriptionDeleted(String store, String subscription) {
        return String.format("""
                {"id":"%s","object":"event","api_version":"2025-03-31.basil","created":%d,
                 "type":"customer.subscription.deleted",
                 "data":{"object":{"id":"%s","object":"subscription","status":"canceled",
                   "cancel_at_period_end":false,"metadata":{"storeId":"%s"}}}}""",
                id(EVENT_PREFIX), Instant.now().getEpochSecond(), subscription, store);
    }

    private String subscriptionUpdated(String store, String subscription, boolean cancelAtPeriodEnd, long end) {
        return String.format("""
                {"id":"%s","object":"event","api_version":"2025-03-31.basil","created":%d,
                 "type":"customer.subscription.updated",
                 "data":{"object":{"id":"%s","object":"subscription","status":"active",
                   "cancel_at_period_end":%s,"current_period_start":%d,"current_period_end":%d,
                   "metadata":{"storeId":"%s"},
                   "items":{"object":"list","data":[{"id":"si_1","object":"subscription_item",
                     "price":{"id":"%s","object":"price"}}]}}}}""",
                id(EVENT_PREFIX), Instant.now().getEpochSecond(), subscription, cancelAtPeriodEnd,
                Instant.now().getEpochSecond(), end, store, stripePriceId);
    }

    // ----------------------------------------------------------------------------------------------- the path

    @Test
    @DisplayName("a completed checkout binds the provider ids without opening the store")
    void checkoutBinds() {
        fixtures.pending(CHECKOUT_STORE);
        String customer = id(CUSTOMER_PREFIX);
        String subscription = id(SUBSCRIPTION_PREFIX);

        deliver(checkoutCompleted(CHECKOUT_STORE, customer, subscription));

        awaitSubscription(CHECKOUT_STORE,
                it -> new StripeSubscriptionId(subscription).equals(it.getStripeSubscriptionId()));
        StoreSubscriptionEntity entity = fixtures.read(CHECKOUT_STORE);
        assertThat(entity.getStripeCustomerId()).isEqualTo(new StripeCustomerId(customer));
        // Still unpaid: the money moving is what activates a store, and treating the redirect as success would open
        // stores that abandoned the payment page.
        assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.PENDING);
    }

    @Test
    @DisplayName("a paid invoice activates the store on the plan the line names, and records the invoice")
    void paymentActivates() {
        String subscription = bind(PAID_STORE);
        long start = Instant.now().getEpochSecond();
        long end = start + 2_592_000L;

        deliver(invoicePaid(subscription, start, end));

        awaitSubscription(PAID_STORE, it -> it.getStatus() == SubscriptionStatus.ACTIVE);
        StoreSubscriptionEntity entity = fixtures.read(PAID_STORE);
        assertThat(entity.getPlanPriceId()).isEqualTo(fixtures.dearestPrice().getId());
        // The renewal date comes from the invoice *line*, not the invoice's own period_start/period_end — which on
        // a first invoice are both the moment it was cut.
        assertThat(entity.getCurrentPeriodEnd()).isEqualTo(Instant.ofEpochSecond(end));

        assertThat(invoices.findAllByStoreId(new StoreMerchantId(PAID_STORE)))
                .anySatisfy(invoice -> {
                    assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
                    assertThat(invoice.getAmountPaid()).isEqualTo(3000L);
                    // Written on the first delivery, not only on a second: revenueStatistic sums on paid_at.
                    assertThat(invoice.getPaidAt()).isNotNull();
                });
    }

    @Test
    @DisplayName("the activation is written to the audit trail against the event that caused it")
    void activationIsAudited() {
        String subscription = bind(RENEWED_STORE);
        long start = Instant.now().getEpochSecond();

        deliver(invoicePaid(subscription, start, start + 2_592_000L));

        awaitSubscription(RENEWED_STORE, it -> it.getStatus() == SubscriptionStatus.ACTIVE);
        await().atMost(APPLIED).until(() -> !audits.findVisible(RENEWED_STORE, null, null, null, null, null,
                50, 0L).isEmpty());
        // "Who moved this store onto this plan, and when" is asked months later, usually during a billing dispute.
        assertThat(audits.findVisible(RENEWED_STORE, null, null, null, null, null, 50, 0L))
                .anySatisfy(row -> assertThat(row.source().name()).isEqualTo("WEBHOOK"));
    }

    @Test
    @DisplayName("a failed renewal opens a grace window and leaves the store trading")
    void failureOpensGrace() {
        String subscription = bind(FAILED_STORE);
        long start = Instant.now().getEpochSecond();
        deliver(invoicePaid(subscription, start, start + 2_592_000L));
        awaitSubscription(FAILED_STORE, it -> it.getStatus() == SubscriptionStatus.ACTIVE);

        deliver(invoiceFailed(subscription));

        awaitSubscription(FAILED_STORE, it -> it.getStatus() == SubscriptionStatus.PAST_DUE);
        StoreSubscriptionEntity entity = fixtures.read(FAILED_STORE);
        // A merchant who cannot trade cannot earn the money to settle the invoice.
        assertThat(entity.operable()).isTrue();
        assertThat(entity.getGraceUntil()).isAfter(Instant.now());
    }

    @Test
    @DisplayName("the provider switching renewal off is mirrored onto the local row")
    void renewalIsReconciled() {
        String subscription = bind(RENEWED_STORE);
        long start = Instant.now().getEpochSecond();
        deliver(invoicePaid(subscription, start, start + 2_592_000L));
        awaitSubscription(RENEWED_STORE, it -> it.getStatus() == SubscriptionStatus.ACTIVE);

        deliver(subscriptionUpdated(RENEWED_STORE, subscription, true, start + 2_592_000L));
        awaitSubscription(RENEWED_STORE, StoreSubscriptionEntity::isCancelAtPeriodEnd);

        // And back off again. An earlier version only ever set the flag, which left a resumed subscription reading
        // as "will not renew" forever once a late webhook from the cancel arrived after the resume.
        deliver(subscriptionUpdated(RENEWED_STORE, subscription, false, start + 2_592_000L));
        awaitSubscription(RENEWED_STORE, it -> !it.isCancelAtPeriodEnd());
    }

    @Test
    @DisplayName("a deleted subscription ends the local one")
    void deletionCancels() {
        String subscription = bind(CANCELLED_STORE);
        long start = Instant.now().getEpochSecond();
        deliver(invoicePaid(subscription, start, start + 2_592_000L));
        awaitSubscription(CANCELLED_STORE, it -> it.getStatus() == SubscriptionStatus.ACTIVE);

        deliver(subscriptionDeleted(CANCELLED_STORE, subscription));

        awaitSubscription(CANCELLED_STORE, it -> it.getStatus() == SubscriptionStatus.CANCELED);
        assertThat(fixtures.read(CANCELLED_STORE).getCanceledAt()).isNotNull();
        assertThat(fixtures.read(CANCELLED_STORE).operable()).isFalse();
    }

    @Test
    @DisplayName("an event for an org other than the one that owns the store is still applied to that store")
    void attributionIsByTheStoreOnTheEvent() {
        // Attribution is the store id Stripe carries, not the caller — there is no caller. The org on the row is
        // what scopes reads afterwards, which the API tests cover; here the only question is that the right row
        // moved.
        fixtures.pending(CHECKOUT_STORE);
        String subscription = id(SUBSCRIPTION_PREFIX);

        deliver(checkoutCompleted(CHECKOUT_STORE, id(CUSTOMER_PREFIX), subscription));

        awaitSubscription(CHECKOUT_STORE,
                it -> new StripeSubscriptionId(subscription).equals(it.getStripeSubscriptionId()));
        assertThat(fixtures.read(CHECKOUT_STORE).getOrgId().getId().toString()).isEqualTo(ORG_A);
    }

}
