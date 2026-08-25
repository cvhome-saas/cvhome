package com.asrevo.cvhome.billing.processors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.billing.commons.errors.IllegalSubscriptionTransitionException;
import com.asrevo.cvhome.billing.commons.errors.PlanPriceNotFoundException;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionNotFoundException;
import com.asrevo.cvhome.billing.events.command.ApplyPendingPlanChangeCommand;
import com.asrevo.cvhome.billing.events.command.ExpireTrialCommand;
import com.asrevo.cvhome.billing.events.command.SuspendUnpaidSubscriptionCommand;
import com.asrevo.cvhome.billing.events.stripe.StripeWebhookReceivedEvent;
import com.asrevo.cvhome.billing.processors.command.ApplyPendingPlanChangeCommandImpl;
import com.asrevo.cvhome.billing.processors.command.ExpireTrialCommandImpl;
import com.asrevo.cvhome.billing.processors.command.SuspendUnpaidSubscriptionCommandImpl;
import com.asrevo.cvhome.billing.processors.event.StripeWebhookReceivedEventImpl;
import com.asrevo.cvhome.billing.service.SubscriptionService;
import com.asrevo.cvhome.billing.service.WebhookApplyService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.errors.UncheckedBaseException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * The outbox handlers, and the one decision each of them makes: whether a failure is worth retrying.
 *
 * <p>
 * That decision is the whole substance here. A subscription that has vanished, or a transition that is no longer
 * legal, will fail identically on every retry — those are facts about our data, so they are logged and the record is
 * allowed to complete rather than burning the outbox's attempts. A price we do not recognise is rethrown, because an
 * unpublished price usually means the catalog sync has not run yet and that resolves on its own.
 * </p>
 */
class BillingProcessorsTest {

    private static final String STORE_ID = "65f023632bc46470c104b76f";

    private static final StoreMerchantId STORE = new StoreMerchantId(STORE_ID);

    private static final String PAYLOAD = "{\"id\":\"obj_1\"}";

    private SubscriptionService subscriptionService;

    private WebhookApplyService applyService;

    @BeforeEach
    void setUp() {
        subscriptionService = mock(SubscriptionService.class);
        applyService = mock(WebhookApplyService.class);
    }

    private static StripeWebhookReceivedEvent event(String type) {
        return StripeWebhookReceivedEvent.of("evt_1", type, STORE_ID, PAYLOAD);
    }

    // -------------------------------------------------------------------------------------------- commands

    @Test
    @DisplayName("expiring a trial delegates to the service")
    void expireTrial() throws Exception {
        new ExpireTrialCommandImpl(subscriptionService).process(ExpireTrialCommand.from(STORE));

        verify(subscriptionService).expireTrial(STORE);
    }

    @Test
    @DisplayName("a trial command for a store that has moved on is swallowed, not retried")
    void expireTrialSwallowsUnretryable() throws Exception {
        doThrow(SubscriptionNotFoundException.forStore(STORE)).when(subscriptionService).expireTrial(STORE);

        // Rethrowing would have the outbox retry until it gave up, for a record that will never succeed.
        assertThatCode(() -> new ExpireTrialCommandImpl(subscriptionService)
                .process(ExpireTrialCommand.from(STORE))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("an illegal transition on a trial command is swallowed too")
    void expireTrialSwallowsIllegalTransition() throws Exception {
        doThrow(IllegalSubscriptionTransitionException.of(STORE, null, "SUSPENDED"))
                .when(subscriptionService).expireTrial(STORE);

        assertThatCode(() -> new ExpireTrialCommandImpl(subscriptionService)
                .process(ExpireTrialCommand.from(STORE))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("suspending an unpaid store delegates, and swallows what will never become true")
    void suspendUnpaid() throws Exception {
        new SuspendUnpaidSubscriptionCommandImpl(subscriptionService)
                .process(SuspendUnpaidSubscriptionCommand.from(STORE));
        verify(subscriptionService).suspendUnpaid(STORE);

        doThrow(SubscriptionNotFoundException.forStore(STORE)).when(subscriptionService).suspendUnpaid(STORE);
        assertThatCode(() -> new SuspendUnpaidSubscriptionCommandImpl(subscriptionService)
                .process(SuspendUnpaidSubscriptionCommand.from(STORE))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("applying a deferred change delegates, and swallows a price the catalog no longer has")
    void applyPendingChange() throws Exception {
        new ApplyPendingPlanChangeCommandImpl(subscriptionService)
                .process(ApplyPendingPlanChangeCommand.from(STORE));
        verify(subscriptionService).applyPendingChange(STORE);

        doThrow(PlanPriceNotFoundException.of("price_gone")).when(subscriptionService).applyPendingChange(STORE);
        // Here the price is one the row already points at, so a retry cannot help — unlike the webhook path below,
        // where the price is one Stripe named and the catalog has yet to publish.
        assertThatCode(() -> new ApplyPendingPlanChangeCommandImpl(subscriptionService)
                .process(ApplyPendingPlanChangeCommand.from(STORE))).doesNotThrowAnyException();
    }

    // ----------------------------------------------------------------------------------------- webhook event

    @Test
    @DisplayName("each handled Stripe type reaches its own apply method")
    void routesEveryHandledType() throws Exception {
        StripeWebhookReceivedEventImpl handler = new StripeWebhookReceivedEventImpl(applyService);

        handler.process(event("checkout.session.completed"));
        handler.process(event("customer.subscription.created"));
        handler.process(event("customer.subscription.updated"));
        handler.process(event("customer.subscription.deleted"));
        handler.process(event("customer.subscription.paused"));
        handler.process(event("invoice.payment_succeeded"));
        handler.process(event("invoice.payment_failed"));

        verify(applyService).applyCheckoutCompleted(eq(STORE), eq("evt_1"), eq(PAYLOAD));
        verify(applyService, org.mockito.Mockito.times(2))
                .applySubscriptionChanged(eq(STORE), anyString(), anyString());
        // paused is routed to "ended" alongside deleted: a paused subscription collects no money, which is the same
        // thing as far as entitlements are concerned.
        verify(applyService, org.mockito.Mockito.times(2))
                .applySubscriptionEnded(eq(STORE), anyString(), anyString());
        verify(applyService).applyInvoicePaid(eq(STORE), eq("evt_1"), eq(PAYLOAD));
        verify(applyService).applyInvoiceFailed(eq(STORE), eq("evt_1"), eq(PAYLOAD));
    }

    @Test
    @DisplayName("a type ingest should never have scheduled is logged rather than acted on")
    void anUnroutedTypeDoesNothing() {
        new StripeWebhookReceivedEventImpl(applyService).process(event("customer.updated"));

        verifyNoInteractions(applyService);
    }

    @Test
    @DisplayName("a store that has vanished ends the record rather than retrying forever")
    void anUnretryableFailureCompletes() throws Exception {
        doThrow(SubscriptionNotFoundException.forStore(STORE))
                .when(applyService).applyInvoicePaid(eq(STORE), anyString(), anyString());

        assertThatCode(() -> new StripeWebhookReceivedEventImpl(applyService)
                .process(event("invoice.payment_succeeded"))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("an illegal transition ends the record too")
    void anIllegalTransitionCompletes() throws Exception {
        doThrow(IllegalSubscriptionTransitionException.of(STORE, null, "ACTIVE"))
                .when(applyService).applySubscriptionChanged(eq(STORE), anyString(), anyString());

        assertThatCode(() -> new StripeWebhookReceivedEventImpl(applyService)
                .process(event("customer.subscription.updated"))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("a price the catalog has not published yet is rethrown, so the outbox retries")
    void anUnpublishedPriceIsRetried() throws Exception {
        doThrow(PlanPriceNotFoundException.of("price_not_synced"))
                .when(applyService).applyInvoicePaid(eq(STORE), anyString(), anyString());

        // The one retryable branch: it normally means the catalog has not been published to Stripe yet, which
        // resolves on the next boot without anyone doing anything.
        assertThatThrownBy(() -> new StripeWebhookReceivedEventImpl(applyService)
                .process(event("invoice.payment_succeeded")))
                .isInstanceOf(UncheckedBaseException.class);
    }

}
