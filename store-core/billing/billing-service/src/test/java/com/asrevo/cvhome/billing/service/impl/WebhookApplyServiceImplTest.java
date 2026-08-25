package com.asrevo.cvhome.billing.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.asrevo.cvhome.billing.commons.AuditEventType;
import com.asrevo.cvhome.billing.commons.BillingInterval;
import com.asrevo.cvhome.billing.commons.InvoiceStatus;
import com.asrevo.cvhome.billing.commons.PlanId;
import com.asrevo.cvhome.billing.commons.PlanPriceId;
import com.asrevo.cvhome.billing.commons.StripeCustomerId;
import com.asrevo.cvhome.billing.commons.StripeEventId;
import com.asrevo.cvhome.billing.commons.StripeInvoiceId;
import com.asrevo.cvhome.billing.commons.StripePriceId;
import com.asrevo.cvhome.billing.commons.StripeScheduleId;
import com.asrevo.cvhome.billing.commons.StripeSubscriptionId;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionNotFoundException;
import com.asrevo.cvhome.billing.config.BillingProperties;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;
import com.asrevo.cvhome.billing.domain.StoreSubscriptionEntity;
import com.asrevo.cvhome.billing.domain.SubscriptionInvoiceEntity;
import com.asrevo.cvhome.billing.repository.StoreSubscriptionRepository;
import com.asrevo.cvhome.billing.repository.SubscriptionInvoiceRepository;
import com.asrevo.cvhome.billing.service.PlanCatalogService;
import com.asrevo.cvhome.billing.service.SubscriptionAuditService;
import com.asrevo.cvhome.billing.service.stripe.StripeEventFixtures;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

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
 * What a verified Stripe event does to the local row.
 *
 * <p>
 * Driven by the same fixtures the ingest test uses, so the shapes are Stripe's rather than a convenient invention.
 * Two things here have bitten before and are pinned deliberately: the period window comes from the invoice
 * <em>line</em> rather than the invoice's own {@code period_start}/{@code period_end}, and the audit row's
 * {@code from_status}/{@code from_plan_id} are read before the entity mutates in place.
 * </p>
 */
class WebhookApplyServiceImplTest {

    private static final String CVH_0001 = "CVH-0001";

    private static final String USD = "USD";

    private static final String CHECKOUT_SESSION_COMPLETED_JSON = "checkout-session-completed.json";

    private static final String CUS_TEST_1 = "cus_test_1";

    private static final String IN_TEST_1 = "in_test_1";

    private static final String INVOICE_PAYMENT_FAILED_JSON = "invoice-payment-failed.json";

    private static final String INVOICE_PAYMENT_SUCCEEDED_JSON = "invoice-payment-succeeded.json";

    private static final String PRICE_PRO_MONTHLY = "price_pro_monthly";

    private static final String SUB_TEST_1 = "sub_test_1";

    private static final String SUBSCRIPTION_DELETED_JSON = "subscription-deleted.json";

    private static final String SUBSCRIPTION_UPDATED_ITEMS_PERIOD_JSON = "subscription-updated-items-period.json";

    private static final String SUBSCRIPTION_UPDATED_JSON = "subscription-updated.json";

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final ManagerOrgId ORG = new ManagerOrgId("32a034a43cd77581d105c87a");

    private static final String EVENT_ID = "evt_1";

    private static final Instant PERIOD_START = Instant.ofEpochSecond(1767225600L);

    private static final Instant PERIOD_END = Instant.ofEpochSecond(1769904000L);

    private StoreSubscriptionRepository subscriptions;

    private SubscriptionInvoiceRepository invoices;

    private PlanCatalogService catalog;

    private SubscriptionAuditService audit;

    private WebhookApplyServiceImpl service;

    private PlanPriceEntity price;

    @BeforeEach
    void setUp() {
        subscriptions = mock(StoreSubscriptionRepository.class);
        invoices = mock(SubscriptionInvoiceRepository.class);
        catalog = mock(PlanCatalogService.class);
        audit = mock(SubscriptionAuditService.class);
        BillingProperties properties = new BillingProperties(Duration.ofDays(14L), Duration.ofDays(7L), null);
        service = new WebhookApplyServiceImpl(subscriptions, invoices, catalog, audit, properties);

        price = PlanPriceEntity.create(PlanId.newId(), new CurrencyCode(USD), 3000L, BillingInterval.MONTH, 0)
                .publishedAs(new StripePriceId(PRICE_PRO_MONTHLY));
        when(subscriptions.save(any(StoreSubscriptionEntity.class)))
                .thenAnswer(it -> it.getArgument(0, StoreSubscriptionEntity.class));
        when(invoices.save(any(SubscriptionInvoiceEntity.class)))
                .thenAnswer(it -> it.getArgument(0, SubscriptionInvoiceEntity.class));
        when(invoices.findById(any(StripeInvoiceId.class))).thenReturn(Optional.empty());
    }

    private void storeIs(StoreSubscriptionEntity entity) {
        when(subscriptions.findById(STORE)).thenReturn(Optional.of(entity));
    }

    private void catalogKnowsThePrice() {
        when(catalog.findByStripePriceId(PRICE_PRO_MONTHLY)).thenReturn(Optional.of(price));
    }

    private static StoreSubscriptionEntity pending() {
        return StoreSubscriptionEntity.pending(STORE, ORG);
    }

    private static StoreSubscriptionEntity active(PlanPriceEntity on) throws Exception {
        return pending().activate(on.getPlanId(), on.getId(), PERIOD_START, PERIOD_END)
                .bindProvider(new StripeCustomerId(CUS_TEST_1), new StripeSubscriptionId(SUB_TEST_1));
    }

    private static String data(String fixture) {
        return StripeEventFixtures.dataObject(fixture);
    }

    private StoreSubscriptionEntity savedSubscription() {
        ArgumentCaptor<StoreSubscriptionEntity> saved = ArgumentCaptor.forClass(StoreSubscriptionEntity.class);
        verify(subscriptions).save(saved.capture());
        return saved.getValue();
    }

    private SubscriptionInvoiceEntity savedInvoice() {
        ArgumentCaptor<SubscriptionInvoiceEntity> saved = ArgumentCaptor.forClass(SubscriptionInvoiceEntity.class);
        verify(invoices).save(saved.capture());
        return saved.getValue();
    }

    // ------------------------------------------------------------------------------------- checkout completed

    @Test
    @DisplayName("a completed checkout binds the provider ids and nothing else")
    void checkoutBindsWithoutActivating() throws Exception {
        storeIs(pending());

        service.applyCheckoutCompleted(STORE, EVENT_ID, data(CHECKOUT_SESSION_COMPLETED_JSON));

        StoreSubscriptionEntity saved = savedSubscription();
        assertThat(saved.getStripeCustomerId()).isEqualTo(new StripeCustomerId(CUS_TEST_1));
        assertThat(saved.getStripeSubscriptionId()).isEqualTo(new StripeSubscriptionId(SUB_TEST_1));
        // The money has not moved yet — invoice.payment_succeeded is what says it has. Activating on the redirect
        // would open stores that abandoned the payment page.
        assertThat(saved.getStatus()).isEqualTo(SubscriptionStatus.PENDING);
    }

    @Test
    @DisplayName("a checkout session carrying no subscription binds nothing")
    void checkoutWithoutASubscriptionIsSkipped() throws Exception {
        storeIs(pending());

        service.applyCheckoutCompleted(STORE, EVENT_ID, data("checkout-session-completed-unattributed.json"));

        verify(subscriptions, never()).save(any(StoreSubscriptionEntity.class));
    }

    @Test
    @DisplayName("an event for a store billing has never seen is reported as missing")
    void anUnknownStoreIsReported() {
        when(subscriptions.findById(STORE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.applyCheckoutCompleted(STORE, EVENT_ID,
                data(CHECKOUT_SESSION_COMPLETED_JSON)))
                .isInstanceOf(SubscriptionNotFoundException.class);
    }

    // ---------------------------------------------------------------------------------- subscription changed

    @Test
    @DisplayName("an active subscription reconciles the period, the schedule and the renewal flag")
    void subscriptionChangedReconciles() throws Exception {
        StoreSubscriptionEntity entity = active(price);
        storeIs(entity);
        catalogKnowsThePrice();

        service.applySubscriptionChanged(STORE, EVENT_ID, data(SUBSCRIPTION_UPDATED_JSON));

        StoreSubscriptionEntity saved = savedSubscription();
        assertThat(saved.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(saved.getCurrentPeriodEnd()).isEqualTo(PERIOD_END);
        assertThat(saved.getStripeScheduleId()).isEqualTo(new StripeScheduleId("sub_sched_test_1"));
        assertThat(saved.isCancelAtPeriodEnd()).isFalse();
    }

    @Test
    @DisplayName("a status that did not move writes no audit row")
    void anUnchangedStatusIsNotAudited() throws Exception {
        storeIs(active(price));
        catalogKnowsThePrice();

        service.applySubscriptionChanged(STORE, EVENT_ID, data(SUBSCRIPTION_UPDATED_JSON));

        // Stripe emits customer.subscription.updated on almost anything. An audit row per delivery would bury the
        // transitions that matter in noise.
        verify(audit, never()).recordFromWebhook(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("the period is read from the item when the account is on an API version that moved it there")
    void readsThePeriodFromTheItem() throws Exception {
        storeIs(active(price));
        catalogKnowsThePrice();

        service.applySubscriptionChanged(STORE, EVENT_ID, data(SUBSCRIPTION_UPDATED_ITEMS_PERIOD_JSON));

        StoreSubscriptionEntity saved = savedSubscription();
        // Stripe moved current_period_* onto items in the 2025 versions, and an account may be pinned to either.
        // Reading only the subscription would record a null renewal date for half the platform.
        assertThat(saved.getStatus()).isEqualTo(SubscriptionStatus.PAST_DUE);
        assertThat(saved.getGraceUntil()).isNotNull();
        assertThat(saved.isCancelAtPeriodEnd()).isTrue();
    }

    @Test
    @DisplayName("a status that moved is audited against the event that caused it")
    void aMovedStatusIsAudited() throws Exception {
        storeIs(active(price));
        catalogKnowsThePrice();

        service.applySubscriptionChanged(STORE, EVENT_ID, data(SUBSCRIPTION_UPDATED_ITEMS_PERIOD_JSON));

        verify(audit).recordFromWebhook(eq(SubscriptionStatus.ACTIVE), any(), any(),
                eq(AuditEventType.PAST_DUE), eq(new StripeEventId(EVENT_ID)));
    }

    @Test
    @DisplayName("a price the catalog has never published is reported rather than guessed at")
    void anUnknownPriceIsReported() throws Exception {
        storeIs(active(price));
        when(catalog.findByStripePriceId(anyString())).thenReturn(Optional.empty());

        // Retryable at the outbox: it normally means the catalog sync has not run yet.
        assertThatThrownBy(() -> service.applySubscriptionChanged(STORE, EVENT_ID, data(SUBSCRIPTION_UPDATED_JSON)))
                .isInstanceOf(com.asrevo.cvhome.billing.commons.errors.PlanPriceNotFoundException.class);
    }

    @Test
    @DisplayName("a subscription event on a row with no provider ids binds them")
    void bindsIdentifiersWhenTheyAreMissing() throws Exception {
        StoreSubscriptionEntity entity = pending()
                .activate(price.getPlanId(), price.getId(), PERIOD_START, PERIOD_END);
        storeIs(entity);
        catalogKnowsThePrice();

        service.applySubscriptionChanged(STORE, EVENT_ID, data(SUBSCRIPTION_UPDATED_JSON));

        StoreSubscriptionEntity saved = savedSubscription();
        assertThat(saved.getStripeSubscriptionId()).isEqualTo(new StripeSubscriptionId(SUB_TEST_1));
        assertThat(saved.getStripeCustomerId()).isEqualTo(new StripeCustomerId(CUS_TEST_1));
    }

    // ------------------------------------------------------------------------------------ subscription ended

    @Test
    @DisplayName("a deleted subscription ends the local one and is audited")
    void subscriptionEnded() throws Exception {
        storeIs(active(price));

        service.applySubscriptionEnded(STORE, EVENT_ID, data(SUBSCRIPTION_DELETED_JSON));

        assertThat(savedSubscription().getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
        verify(audit).recordFromWebhook(eq(SubscriptionStatus.ACTIVE), any(), any(), eq(AuditEventType.CANCELED),
                eq(new StripeEventId(EVENT_ID)));
    }

    @Test
    @DisplayName("a redelivered deletion writes nothing at all")
    void anAlreadyCancelledSubscriptionIsUntouched() throws Exception {
        StoreSubscriptionEntity entity = active(price);
        entity.cancelNow(PERIOD_END);
        storeIs(entity);

        service.applySubscriptionEnded(STORE, EVENT_ID, data(SUBSCRIPTION_DELETED_JSON));

        // Not even a save: a second audit row for one cancellation is a lie about what happened.
        verify(subscriptions, never()).save(any(StoreSubscriptionEntity.class));
        verify(audit, never()).recordFromWebhook(any(), any(), any(), any(), any());
    }

    // ------------------------------------------------------------------------------------------ invoice paid

    @Test
    @DisplayName("a paid invoice activates the store on the plan the line was for")
    void invoicePaidActivates() throws Exception {
        storeIs(pending());
        catalogKnowsThePrice();

        service.applyInvoicePaid(STORE, EVENT_ID, data(INVOICE_PAYMENT_SUCCEEDED_JSON));

        StoreSubscriptionEntity saved = savedSubscription();
        assertThat(saved.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(saved.getPlanPriceId()).isEqualTo(price.getId());
        verify(audit).recordFromWebhook(eq(SubscriptionStatus.PENDING), any(), any(), eq(AuditEventType.ACTIVATED),
                any());
    }

    @Test
    @DisplayName("the paid period comes from the invoice line, not the invoice's own dates")
    void periodComesFromTheLine() throws Exception {
        storeIs(pending());
        catalogKnowsThePrice();

        service.applyInvoicePaid(STORE, EVENT_ID, data(INVOICE_PAYMENT_SUCCEEDED_JSON));

        StoreSubscriptionEntity saved = savedSubscription();
        // The fixture's invoice-level period_start and period_end are both the moment it was cut — which is what
        // Stripe puts on a subscription's first invoice. Reading those made a monthly subscription report a renewal
        // date of today. The line carries the real window.
        assertThat(saved.getCurrentPeriodStart()).isEqualTo(PERIOD_START);
        assertThat(saved.getCurrentPeriodEnd()).isEqualTo(PERIOD_END);
    }

    @Test
    @DisplayName("the invoice-level dates are still the fallback for an invoice with no lines")
    void periodFallsBackToTheInvoice() throws Exception {
        storeIs(pending());
        when(catalog.findByStripePriceId(anyString())).thenReturn(Optional.empty());
        when(catalog.requirePrice(any(PlanPriceId.class))).thenReturn(price);
        StoreSubscriptionEntity onAPlan = pending()
                .activate(price.getPlanId(), price.getId(), PERIOD_START, PERIOD_END);
        storeIs(onAPlan);

        service.applyInvoicePaid(STORE, EVENT_ID, data("invoice-payment-succeeded-legacy.json"));

        assertThat(savedSubscription().getCurrentPeriodEnd()).isEqualTo(PERIOD_END);
    }

    @Test
    @DisplayName("a renewal of an already active store is audited as a renewal, not an activation")
    void aRenewalIsAudited() throws Exception {
        storeIs(active(price));
        catalogKnowsThePrice();

        service.applyInvoicePaid(STORE, EVENT_ID, data(INVOICE_PAYMENT_SUCCEEDED_JSON));

        verify(audit).recordFromWebhook(eq(SubscriptionStatus.ACTIVE), any(), any(), eq(AuditEventType.RENEWED),
                any());
    }

    @Test
    @DisplayName("the invoice row records what was billed, where to read it and when it was paid")
    void recordsTheInvoice() throws Exception {
        storeIs(active(price));
        catalogKnowsThePrice();

        service.applyInvoicePaid(STORE, EVENT_ID, data(INVOICE_PAYMENT_SUCCEEDED_JSON));

        SubscriptionInvoiceEntity saved = savedInvoice();
        assertThat(saved.getId()).isEqualTo(new StripeInvoiceId(IN_TEST_1));
        assertThat(saved.getInvoiceNumber()).isEqualTo(CVH_0001);
        assertThat(saved.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(saved.amountDue().minorUnits()).isEqualTo(3000L);
        assertThat(saved.amountPaid().minorUnits()).isEqualTo(3000L);
        assertThat(saved.getCurrency()).isEqualTo(new CurrencyCode(USD));
        // The regression this pins: `record` writes no paid_at, so a renewal that succeeded on its first delivery
        // was stored PAID with a null paid_at — and revenueStatistic sums `where status = 'PAID' and paid_at >=
        // :from`. Every such invoice, which is most of them, was missing from platform revenue.
        assertThat(saved.getPaidAt()).isNotNull();
        assertThat(saved.getHostedInvoiceUrl()).isEqualTo("https://invoice.stripe.test/in_test_1");
        assertThat(saved.getInvoicePdfUrl()).isEqualTo("https://invoice.stripe.test/in_test_1.pdf");
        assertThat(saved.getPeriodStart()).isEqualTo(PERIOD_START);
        assertThat(saved.getPeriodEnd()).isEqualTo(PERIOD_END);
    }

    @Test
    @DisplayName("an invoice seen before is updated in place rather than duplicated")
    void anExistingInvoiceIsSettled() throws Exception {
        storeIs(active(price));
        catalogKnowsThePrice();
        SubscriptionInvoiceEntity existing = SubscriptionInvoiceEntity.record(new StripeInvoiceId(IN_TEST_1),
                STORE, ORG, new StripeSubscriptionId(SUB_TEST_1), CVH_0001, InvoiceStatus.OPEN,
                new com.asrevo.cvhome.billing.commons.Money(new CurrencyCode(USD), 3000L), 0L, PERIOD_START);
        when(invoices.findById(new StripeInvoiceId(IN_TEST_1))).thenReturn(Optional.of(existing));

        service.applyInvoicePaid(STORE, EVENT_ID, data(INVOICE_PAYMENT_SUCCEEDED_JSON));

        // A failure then a success is one invoice with two states, not two rows of history that never happened.
        SubscriptionInvoiceEntity saved = savedInvoice();
        assertThat(saved).isSameAs(existing);
        assertThat(saved.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(saved.getAmountPaid()).isEqualTo(3000L);
    }

    // ---------------------------------------------------------------------------------------- invoice failed

    @Test
    @DisplayName("a failed renewal opens the configured grace window and leaves the store usable")
    void invoiceFailedOpensGrace() throws Exception {
        storeIs(active(price));
        Instant before = Instant.now();

        service.applyInvoiceFailed(STORE, EVENT_ID, data(INVOICE_PAYMENT_FAILED_JSON));

        StoreSubscriptionEntity saved = savedSubscription();
        assertThat(saved.getStatus()).isEqualTo(SubscriptionStatus.PAST_DUE);
        assertThat(saved.operable()).isTrue();
        // Seven days, from BillingProperties.pastDueGrace.
        assertThat(saved.getGraceUntil()).isAfterOrEqualTo(before.plus(Duration.ofDays(7L)).minusSeconds(5));
        verify(audit).recordFromWebhook(eq(SubscriptionStatus.ACTIVE), any(), any(),
                eq(AuditEventType.PAYMENT_FAILED), eq(new StripeEventId(EVENT_ID)));
    }

    @Test
    @DisplayName("a failed invoice is recorded as open and unpaid, not as paid for zero")
    void aFailedInvoiceIsOpen() throws Exception {
        storeIs(active(price));

        service.applyInvoiceFailed(STORE, EVENT_ID, data(INVOICE_PAYMENT_FAILED_JSON));

        SubscriptionInvoiceEntity saved = savedInvoice();
        assertThat(saved.getStatus()).isEqualTo(InvoiceStatus.OPEN);
        assertThat(saved.amountPaid().minorUnits()).isZero();
        assertThat(saved.getPaidAt()).isNull();
    }

}
