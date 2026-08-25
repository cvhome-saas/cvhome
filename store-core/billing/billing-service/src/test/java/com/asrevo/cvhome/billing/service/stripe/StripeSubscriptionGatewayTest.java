package com.asrevo.cvhome.billing.service.stripe;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.asrevo.cvhome.billing.commons.BillingInterval;
import com.asrevo.cvhome.billing.commons.PlanId;
import com.asrevo.cvhome.billing.commons.StripePriceId;
import com.asrevo.cvhome.billing.commons.StripeScheduleId;
import com.asrevo.cvhome.billing.commons.StripeSubscriptionId;
import com.asrevo.cvhome.billing.commons.errors.BillingProviderUnavailableException;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionChangeRejectedException;
import com.asrevo.cvhome.billing.config.StripeCredentials;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;
import com.asrevo.cvhome.billing.repository.StripeRequestRepository;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.stripe.StripeClient;
import com.stripe.exception.ApiConnectionException;
import com.stripe.exception.CardException;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import com.stripe.model.SubscriptionItemCollection;
import com.stripe.model.SubscriptionSchedule;
import com.stripe.net.RequestOptions;
import com.stripe.param.SubscriptionCancelParams;
import com.stripe.param.SubscriptionScheduleCreateParams;
import com.stripe.param.SubscriptionScheduleUpdateParams;
import com.stripe.param.SubscriptionUpdateParams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Changing a subscription that already exists.
 *
 * <p>
 * Upgrades and downgrades are deliberately not symmetric, because money makes them different: moving up is charged
 * now so it happens now, moving down would take away something already paid for so it waits. Most of what follows is
 * about keeping that asymmetry — and the exception mapping that decides whether a customer is told their card failed
 * — from being flattened by a later edit.
 * </p>
 *
 * <p>
 * Every fixture is built into a local <em>before</em> the {@code when(...)} that returns it. Building a mock inside
 * a stubbing argument leaves Mockito mid-stub and fails with {@code UnfinishedStubbingException}, which reads like a
 * bug in the code under test rather than in the test.
 * </p>
 */
class StripeSubscriptionGatewayTest {

    private static final String PERIOD_END_TEXT = "2026-02-01T00:00:00Z";

    private static final String ACTIVE = "active";

    private static final String NO_ROUTE_TO_STRIPE = "no route to stripe";

    private static final String NOT_STARTED = "not_started";

    private static final String SI_1 = "si_1";

    private static final String SK_TEST_SUBSCRIPTION = "sk_test_subscription";

    private static final String SUB_1 = "sub_1";

    private static final String SUB_SCHED_UPDATED = "sub_sched_updated";

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final StripeSubscriptionId SUBSCRIPTION = new StripeSubscriptionId(SUB_1);

    private static final StripeScheduleId SCHEDULE = new StripeScheduleId("sub_sched_1");

    private static final String TARGET_PRICE = "price_target";

    private static final String CURRENT_PRICE = "price_current";

    private static final long PHASE_START = 1735689600L;

    private StripeClient stripe;

    private StripeSubscriptionGateway gateway;

    @BeforeEach
    void setUp() {
        StripeCredentials credentials = mock(StripeCredentials.class);
        when(credentials.apiKey()).thenReturn(SK_TEST_SUBSCRIPTION);
        stripe = mock(StripeClient.class, RETURNS_DEEP_STUBS);
        StripeRequestRepository requests = mock(StripeRequestRepository.class);
        when(requests.existsById(anyString())).thenReturn(false);
        when(requests.findById(anyString())).thenReturn(Optional.empty());
        gateway = new StripeSubscriptionGateway(credentials, requests, stripe);
    }

    // -------------------------------------------------------------------------------------------------- fixtures

    private static PlanPriceEntity target() {
        PlanPriceEntity price = PlanPriceEntity.create(PlanId.newId(), new CurrencyCode("USD"), 3000L,
                BillingInterval.MONTH, 0);
        return price.publishedAs(new StripePriceId(TARGET_PRICE));
    }

    /** A retrieved subscription carrying one item, which is the only shape this service ever creates. */
    private static Subscription subscriptionWithItem(String itemId, String schedule) {
        Subscription subscription = mock(Subscription.class);
        SubscriptionItem item = mock(SubscriptionItem.class);
        when(item.getId()).thenReturn(itemId);
        SubscriptionItemCollection items = mock(SubscriptionItemCollection.class);
        when(items.getData()).thenReturn(List.of(item));
        when(subscription.getItems()).thenReturn(items);
        when(subscription.getSchedule()).thenReturn(schedule);
        return subscription;
    }

    private static SubscriptionSchedule scheduleWithPhase(String status) {
        SubscriptionSchedule schedule = mock(SubscriptionSchedule.class);
        when(schedule.getId()).thenReturn(SCHEDULE.id());
        when(schedule.getStatus()).thenReturn(status);
        SubscriptionSchedule.Phase phase = mock(SubscriptionSchedule.Phase.class);
        SubscriptionSchedule.Phase.Item item = mock(SubscriptionSchedule.Phase.Item.class);
        when(item.getPrice()).thenReturn(CURRENT_PRICE);
        when(phase.getItems()).thenReturn(List.of(item));
        when(phase.getStartDate()).thenReturn(PHASE_START);
        when(schedule.getPhases()).thenReturn(List.of(phase));
        return schedule;
    }

    private static SubscriptionSchedule scheduleNamed(String id) {
        SubscriptionSchedule schedule = mock(SubscriptionSchedule.class);
        when(schedule.getId()).thenReturn(id);
        return schedule;
    }

    private void retrieveReturns(Subscription subscription) throws Exception {
        when(stripe.subscriptions().retrieve(eq(SUB_1), any(RequestOptions.class))).thenReturn(subscription);
    }

    private void scheduleRetrieveReturns(SubscriptionSchedule schedule) throws Exception {
        when(stripe.subscriptionSchedules().retrieve(eq(SCHEDULE.id()), any(RequestOptions.class)))
                .thenReturn(schedule);
    }

    // -------------------------------------------------------------------------------------------------- upgrade

    @Test
    @DisplayName("an upgrade swaps the item's price and settles the proration invoice synchronously")
    void upgradeChargesNow() throws Exception {
        retrieveReturns(subscriptionWithItem(SI_1, null));

        gateway.upgradeNow(STORE, SUBSCRIPTION, target());

        ArgumentCaptor<SubscriptionUpdateParams> params = ArgumentCaptor.forClass(SubscriptionUpdateParams.class);
        verify(stripe.subscriptions()).update(eq(SUB_1), params.capture(), any(RequestOptions.class));
        assertThat(params.getValue().getItems()).singleElement().satisfies(item -> {
            // The existing item is replaced, not added to — a second item would double the bill.
            assertThat(item.getId()).isEqualTo(SI_1);
            assertThat(item.getPrice()).isEqualTo(TARGET_PRICE);
        });
        // Together these are what make a declined card come back as a CardException here rather than leaving the
        // customer on the new plan with an unpaid invoice.
        assertThat(params.getValue().getProrationBehavior())
                .isEqualTo(SubscriptionUpdateParams.ProrationBehavior.ALWAYS_INVOICE);
        assertThat(params.getValue().getPaymentBehavior())
                .isEqualTo(SubscriptionUpdateParams.PaymentBehavior.ERROR_IF_INCOMPLETE);
    }

    @Test
    @DisplayName("an upgrade a card refuses is reported as a rejection, so the caller can refuse it cleanly")
    void upgradeDeclinedIsARejection() throws Exception {
        retrieveReturns(subscriptionWithItem(SI_1, null));
        when(stripe.subscriptions().update(anyString(), any(SubscriptionUpdateParams.class),
                any(RequestOptions.class)))
                .thenThrow(new CardException("declined", "req_1", "card_declined", null, "generic_decline", null,
                        402, null));

        assertThatThrownBy(() -> gateway.upgradeNow(STORE, SUBSCRIPTION, target()))
                .isInstanceOf(SubscriptionChangeRejectedException.class);
    }

    @Test
    @DisplayName("an upgrade Stripe never answered settles nothing")
    void upgradeUnreachableIsUnavailable() throws Exception {
        when(stripe.subscriptions().retrieve(eq(SUB_1), any(RequestOptions.class)))
                .thenThrow(new ApiConnectionException(NO_ROUTE_TO_STRIPE));

        // The caller must not write a local plan change on this: the change may or may not have landed.
        assertThatThrownBy(() -> gateway.upgradeNow(STORE, SUBSCRIPTION, target()))
                .isInstanceOf(BillingProviderUnavailableException.class);
    }

    // ------------------------------------------------------------------------------------------------ downgrade

    @Test
    @DisplayName("a downgrade writes two phases: the current price to the boundary, then the cheaper one")
    void downgradeBuildsTwoPhases() throws Exception {
        Instant effectiveAt = Instant.parse(PERIOD_END_TEXT);
        retrieveReturns(subscriptionWithItem(SI_1, null));
        SubscriptionSchedule fresh = scheduleWithPhase(NOT_STARTED);
        when(stripe.subscriptionSchedules().create(any(SubscriptionScheduleCreateParams.class),
                any(RequestOptions.class))).thenReturn(fresh);
        SubscriptionSchedule updated = scheduleNamed(SUB_SCHED_UPDATED);
        when(stripe.subscriptionSchedules().update(anyString(), any(SubscriptionScheduleUpdateParams.class),
                any(RequestOptions.class))).thenReturn(updated);

        StripeScheduleId id = gateway.scheduleDowngrade(STORE, SUBSCRIPTION, target(), effectiveAt);

        ArgumentCaptor<SubscriptionScheduleUpdateParams> params =
                ArgumentCaptor.forClass(SubscriptionScheduleUpdateParams.class);
        verify(stripe.subscriptionSchedules()).update(eq(SCHEDULE.id()), params.capture(), any(RequestOptions.class));
        assertThat(id).isEqualTo(new StripeScheduleId(SUB_SCHED_UPDATED));
        assertThat(params.getValue().getPhases()).hasSize(2);
        var first = params.getValue().getPhases().getFirst();
        var second = params.getValue().getPhases().get(1);
        // The customer keeps what they paid for right up to the boundary.
        assertThat(first.getItems().getFirst().getPrice()).isEqualTo(CURRENT_PRICE);
        assertThat(first.getStartDate()).isEqualTo(PHASE_START);
        assertThat(first.getEndDate()).isEqualTo(effectiveAt.getEpochSecond());
        // The cheaper price simply becomes the ongoing one — no end date.
        assertThat(second.getItems().getFirst().getPrice()).isEqualTo(TARGET_PRICE);
        assertThat(second.getEndDate()).isNull();
        // Released when it gets there, so the subscription goes back to ordinary renewal instead of living under a
        // schedule forever.
        assertThat(params.getValue().getEndBehavior())
                .isEqualTo(SubscriptionScheduleUpdateParams.EndBehavior.RELEASE);
    }

    @Test
    @DisplayName("a subscription that already has a schedule reuses it rather than creating a second")
    void downgradeReusesAnExistingSchedule() throws Exception {
        retrieveReturns(subscriptionWithItem(SI_1, SCHEDULE.id()));
        scheduleRetrieveReturns(scheduleWithPhase(ACTIVE));
        SubscriptionSchedule updated = scheduleNamed(SCHEDULE.id());
        when(stripe.subscriptionSchedules().update(anyString(), any(SubscriptionScheduleUpdateParams.class),
                any(RequestOptions.class))).thenReturn(updated);

        gateway.scheduleDowngrade(STORE, SUBSCRIPTION, target(), Instant.parse(PERIOD_END_TEXT));

        // Not an optimisation: Stripe refuses a second schedule for one subscription, so without this a downgrade
        // could never be re-requested or corrected once one existed.
        verify(stripe.subscriptionSchedules(), never())
                .create(any(SubscriptionScheduleCreateParams.class), any(RequestOptions.class));
        verify(stripe.subscriptionSchedules()).retrieve(eq(SCHEDULE.id()), any(RequestOptions.class));
    }

    @Test
    @DisplayName("a downgrade has no card branch, because nothing is charged now")
    void downgradeFailureIsAlwaysUnavailable() throws Exception {
        when(stripe.subscriptions().retrieve(eq(SUB_1), any(RequestOptions.class)))
                .thenThrow(new ApiConnectionException(NO_ROUTE_TO_STRIPE));

        assertThatThrownBy(() -> gateway.scheduleDowngrade(STORE, SUBSCRIPTION, target(), Instant.now()))
                .isInstanceOf(BillingProviderUnavailableException.class);
    }

    // -------------------------------------------------------------------------------------------------- release

    @Test
    @DisplayName("a live schedule is released, not cancelled")
    void releasesALiveSchedule() throws Exception {
        scheduleRetrieveReturns(scheduleWithPhase(ACTIVE));

        gateway.releaseSchedule(STORE, SCHEDULE);

        // Cancelling a schedule would end the subscription with it, which is emphatically not what calling off a
        // downgrade means.
        verify(stripe.subscriptionSchedules()).release(eq(SCHEDULE.id()), any(RequestOptions.class));
    }

    @Test
    @DisplayName("a not-yet-started schedule is releasable too")
    void releasesANotStartedSchedule() throws Exception {
        scheduleRetrieveReturns(scheduleWithPhase(NOT_STARTED));

        gateway.releaseSchedule(STORE, SCHEDULE);

        verify(stripe.subscriptionSchedules()).release(eq(SCHEDULE.id()), any(RequestOptions.class));
    }

    @Test
    @DisplayName("a schedule that is already released is left alone, and is not a failure")
    void releasingAReleasedScheduleIsANoOp() throws Exception {
        scheduleRetrieveReturns(scheduleWithPhase("released"));

        gateway.releaseSchedule(STORE, SCHEDULE);

        // The end state this asks for already holds. Treating it as a failure would turn a retry — or a local row
        // that lagged behind — into a dead end.
        verify(stripe.subscriptionSchedules(), never()).release(anyString(), any(RequestOptions.class));
    }

    @Test
    @DisplayName("a schedule that ran its course is left alone too")
    void releasingACompletedScheduleIsANoOp() throws Exception {
        scheduleRetrieveReturns(scheduleWithPhase("completed"));

        gateway.releaseSchedule(STORE, SCHEDULE);

        verify(stripe.subscriptionSchedules(), never()).release(anyString(), any(RequestOptions.class));
    }

    @Test
    @DisplayName("a release Stripe would not answer is a provider fault")
    void releaseFailureIsUnavailable() throws Exception {
        when(stripe.subscriptionSchedules().retrieve(eq(SCHEDULE.id()), any(RequestOptions.class)))
                .thenThrow(new ApiConnectionException(NO_ROUTE_TO_STRIPE));

        assertThatThrownBy(() -> gateway.releaseSchedule(STORE, SCHEDULE))
                .isInstanceOf(BillingProviderUnavailableException.class);
    }

    // -------------------------------------------------------------------------------------------------- renewal

    @Test
    @DisplayName("switching renewal off sets cancel_at_period_end rather than ending anything")
    void setRenewalOff() throws Exception {
        gateway.setRenewal(STORE, SUBSCRIPTION, false);

        ArgumentCaptor<SubscriptionUpdateParams> params = ArgumentCaptor.forClass(SubscriptionUpdateParams.class);
        verify(stripe.subscriptions()).update(eq(SUB_1), params.capture(), any(RequestOptions.class));
        // The subscription stays active and the customer keeps everything until the period they paid for runs out.
        assertThat(params.getValue().getCancelAtPeriodEnd()).isTrue();
    }

    @Test
    @DisplayName("switching renewal back on clears the flag")
    void setRenewalOn() throws Exception {
        gateway.setRenewal(STORE, SUBSCRIPTION, true);

        ArgumentCaptor<SubscriptionUpdateParams> params = ArgumentCaptor.forClass(SubscriptionUpdateParams.class);
        verify(stripe.subscriptions()).update(eq(SUB_1), params.capture(), any(RequestOptions.class));
        assertThat(params.getValue().getCancelAtPeriodEnd()).isFalse();
    }

    @Test
    @DisplayName("the two renewal directions do not share an idempotency key")
    void renewalKeysDiffer() throws Exception {
        gateway.setRenewal(STORE, SUBSCRIPTION, false);
        gateway.setRenewal(STORE, SUBSCRIPTION, true);

        ArgumentCaptor<RequestOptions> options = ArgumentCaptor.forClass(RequestOptions.class);
        verify(stripe.subscriptions(), times(2))
                .update(eq(SUB_1), any(SubscriptionUpdateParams.class), options.capture());
        // Cancel then resume inside the same minute is a real sequence. A shared key would have Stripe replay the
        // stored answer to the first and silently drop the second.
        assertThat(options.getAllValues().getFirst().getIdempotencyKey())
                .isNotEqualTo(options.getAllValues().getLast().getIdempotencyKey());
        assertThat(options.getAllValues().getFirst().getIdempotencyKey()).contains("sub_1:renew:false");
        assertThat(options.getAllValues().getLast().getIdempotencyKey()).contains("sub_1:renew:true");
    }

    @Test
    @DisplayName("a renewal switch Stripe would not answer is a provider fault")
    void setRenewalFailureIsUnavailable() throws Exception {
        when(stripe.subscriptions().update(anyString(), any(SubscriptionUpdateParams.class),
                any(RequestOptions.class)))
                .thenThrow(new ApiConnectionException(NO_ROUTE_TO_STRIPE));

        assertThatThrownBy(() -> gateway.setRenewal(STORE, SUBSCRIPTION, false))
                .isInstanceOf(BillingProviderUnavailableException.class);
    }

    // --------------------------------------------------------------------------------------------------- cancel

    @Test
    @DisplayName("an immediate cancel ends the subscription at Stripe")
    void cancelNow() throws Exception {
        gateway.cancelNow(STORE, SUBSCRIPTION);

        verify(stripe.subscriptions()).cancel(eq(SUB_1), any(SubscriptionCancelParams.class),
                any(RequestOptions.class));
    }

    @Test
    @DisplayName("a cancel Stripe would not answer is a provider fault")
    void cancelFailureIsUnavailable() throws Exception {
        when(stripe.subscriptions().cancel(anyString(), any(SubscriptionCancelParams.class),
                any(RequestOptions.class)))
                .thenThrow(new ApiConnectionException(NO_ROUTE_TO_STRIPE));

        assertThatThrownBy(() -> gateway.cancelNow(STORE, SUBSCRIPTION))
                .isInstanceOf(BillingProviderUnavailableException.class);
    }

    // ------------------------------------------------------------------------------------------ request options

    @Test
    @DisplayName("reads carry no idempotency key, so the request table records intent rather than traffic")
    void readsAreNotKeyed() throws Exception {
        retrieveReturns(subscriptionWithItem(SI_1, null));

        gateway.upgradeNow(STORE, SUBSCRIPTION, target());

        ArgumentCaptor<RequestOptions> read = ArgumentCaptor.forClass(RequestOptions.class);
        verify(stripe.subscriptions()).retrieve(eq(SUB_1), read.capture());
        assertThat(read.getValue().getIdempotencyKey()).isNull();
        assertThat(read.getValue().getApiKey()).isEqualTo(SK_TEST_SUBSCRIPTION);
    }

}
