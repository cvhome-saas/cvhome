package com.asrevo.cvhome.billing.service.impl;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.asrevo.cvhome.billing.commons.AuditEventType;
import com.asrevo.cvhome.billing.commons.BillingInterval;
import com.asrevo.cvhome.billing.commons.ChangeSource;
import com.asrevo.cvhome.billing.commons.PlanId;
import com.asrevo.cvhome.billing.commons.PlanPriceId;
import com.asrevo.cvhome.billing.commons.StripeCustomerId;
import com.asrevo.cvhome.billing.commons.StripePriceId;
import com.asrevo.cvhome.billing.commons.StripeScheduleId;
import com.asrevo.cvhome.billing.commons.StripeSubscriptionId;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.commons.dto.CheckoutSessionView;
import com.asrevo.cvhome.billing.commons.dto.SubscriptionView;
import com.asrevo.cvhome.billing.commons.errors.BillingProviderUnavailableException;
import com.asrevo.cvhome.billing.commons.errors.IllegalSubscriptionTransitionException;
import com.asrevo.cvhome.billing.commons.errors.ImmediateCancelForbiddenException;
import com.asrevo.cvhome.billing.commons.errors.PlanPriceNotFoundException;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionNotFoundException;
import com.asrevo.cvhome.billing.domain.PlanEntity;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;
import com.asrevo.cvhome.billing.domain.StoreSubscriptionEntity;
import com.asrevo.cvhome.billing.mappers.SubscriptionMappers;
import com.asrevo.cvhome.billing.repository.StoreSubscriptionRepository;
import com.asrevo.cvhome.billing.service.PlanCatalogService;
import com.asrevo.cvhome.billing.service.SubscriptionAuditService;
import com.asrevo.cvhome.billing.service.stripe.StripeCheckoutGateway;
import com.asrevo.cvhome.billing.service.stripe.StripeCustomerGateway;
import com.asrevo.cvhome.billing.service.stripe.StripeSubscriptionGateway;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Buying, moving between plans, stopping and restarting.
 *
 * <p>
 * The ordering rule is what most of this is about: Stripe is called first and the local row written only once it has
 * answered, so a refusal leaves the customer exactly where they were and an <em>unknown</em> outcome leaves the row
 * untouched for the webhook to settle. Writing locally first and reconciling later would show a customer a plan they
 * had not bought.
 * </p>
 *
 * <p>
 * The audit trail is checked as carefully as the state, because {@code from_status} and {@code from_plan_id} have to
 * be captured before the aggregate mutates in place — reading them after the save answers the plan the store moved
 * <em>to</em>, which is how {@code from_plan_id} came to be a literal null on every row on the platform.
 * </p>
 */
class SubscriptionServiceImplTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final ManagerOrgId ORG = new ManagerOrgId("32a034a43cd77581d105c87a");

    private static final ManagerOrgId OTHER_ORG = new ManagerOrgId("42a034a43cd77581d105c87b");

    private static final String ACTOR = "owner@example.test";

    private static final Instant PERIOD_START = Instant.parse("2026-01-01T00:00:00Z");

    private static final Instant PERIOD_END = Instant.parse("2026-02-01T00:00:00Z");

    private StoreSubscriptionRepository subscriptions;

    private PlanCatalogService catalog;

    private StripeCustomerGateway customerGateway;

    private StripeCheckoutGateway checkoutGateway;

    private StripeSubscriptionGateway subscriptionGateway;

    private SubscriptionAuditService audit;

    private SubscriptionMappers mappers;

    private SubscriptionServiceImpl service;

    private PlanEntity basicPlan;

    private PlanEntity proPlan;

    private PlanPriceEntity basicMonthly;

    private PlanPriceEntity proMonthly;

    @BeforeEach
    void setUp() {
        subscriptions = mock(StoreSubscriptionRepository.class);
        catalog = mock(PlanCatalogService.class);
        customerGateway = mock(StripeCustomerGateway.class);
        checkoutGateway = mock(StripeCheckoutGateway.class);
        subscriptionGateway = mock(StripeSubscriptionGateway.class);
        audit = mock(SubscriptionAuditService.class);
        mappers = mock(SubscriptionMappers.class);
        service = new SubscriptionServiceImpl(subscriptions, catalog, customerGateway, checkoutGateway,
                subscriptionGateway, audit, mappers);

        basicPlan = PlanEntity.create("BASIC", "Basic", "For a store finding its feet.", 10);
        proPlan = PlanEntity.create("PRO", "Pro", "For a store that is growing.", 20);
        basicMonthly = PlanPriceEntity.create(basicPlan.getId(), new CurrencyCode("USD"), 1000L,
                BillingInterval.MONTH, 0).publishedAs(new StripePriceId("price_basic"));
        proMonthly = PlanPriceEntity.create(proPlan.getId(), new CurrencyCode("USD"), 3000L,
                BillingInterval.MONTH, 0).publishedAs(new StripePriceId("price_pro"));

        when(catalog.findPlan(basicPlan.getId())).thenReturn(Optional.of(basicPlan));
        when(catalog.findPlan(proPlan.getId())).thenReturn(Optional.of(proPlan));
        when(subscriptions.save(any(StoreSubscriptionEntity.class)))
                .thenAnswer(it -> it.getArgument(0, StoreSubscriptionEntity.class));
        when(mappers.toView(any(StoreSubscriptionEntity.class))).thenAnswer(it -> view(
                it.getArgument(0, StoreSubscriptionEntity.class)));
    }

    private static SubscriptionView view(StoreSubscriptionEntity entity) {
        return new SubscriptionView(entity.getId(), entity.getStatus(), null, null, entity.getPlanPriceId(), null,
                entity.getCurrentPeriodEnd(), entity.getTrialEnd(), entity.isCancelAtPeriodEnd(),
                entity.getGraceUntil(), null, entity.getStripeSubscriptionId() != null, Map.of());
    }

    private StoreSubscriptionEntity onBasic() throws Exception {
        StoreSubscriptionEntity entity = StoreSubscriptionEntity.pending(STORE, ORG)
                .activate(basicPlan.getId(), basicMonthly.getId(), PERIOD_START, PERIOD_END);
        return entity.bindProvider(new StripeCustomerId("cus_1"), new StripeSubscriptionId("sub_1"));
    }

    /** The row every read finds, in both the scoped and unscoped queries. */
    private void inRepository(StoreSubscriptionEntity entity) {
        when(subscriptions.findById(STORE)).thenReturn(Optional.of(entity));
        when(subscriptions.findByIdAndOrgId(STORE, ORG)).thenReturn(Optional.of(entity));
        when(subscriptions.findByIdAndOrgId(STORE, OTHER_ORG)).thenReturn(Optional.empty());
    }

    private StoreSubscriptionEntity lastSaved() {
        ArgumentCaptor<StoreSubscriptionEntity> saved = ArgumentCaptor.forClass(StoreSubscriptionEntity.class);
        verify(subscriptions, org.mockito.Mockito.atLeastOnce()).save(saved.capture());
        return saved.getValue();
    }

    // -------------------------------------------------------------------------------------------- tenant scope

    @Nested
    @DisplayName("tenant scope")
    class TenantScope {

        @Test
        @DisplayName("an org-scoped read goes through the query that names the org")
        void anOrgScopedReadIsNarrowed() throws Exception {
            inRepository(onBasic());

            service.current(STORE, ORG);

            // Not findById plus a check afterwards: the boundary lives in the query, because the shared permission
            // checker cannot tell which org a store belongs to and returns true for any store an org admin asks for.
            verify(subscriptions).findByIdAndOrgId(STORE, ORG);
            verify(subscriptions, never()).findById(STORE);
        }

        @Test
        @DisplayName("another org's admin cannot read this store's subscription")
        void anotherOrgCannotRead() throws Exception {
            inRepository(onBasic());

            assertThatThrownBy(() -> service.current(STORE, OTHER_ORG))
                    .isInstanceOf(SubscriptionNotFoundException.class);
        }

        @Test
        @DisplayName("a null scope spans orgs — a platform operator or another cvhome service")
        void aNullScopeSpansOrgs() throws Exception {
            inRepository(onBasic());

            service.current(STORE, null);

            verify(subscriptions).findById(STORE);
            verify(subscriptions, never()).findByIdAndOrgId(any(), any());
        }

        @Test
        @DisplayName("another org's admin cannot change this store's plan either")
        void anotherOrgCannotChangePlan() throws Exception {
            inRepository(onBasic());

            assertThatThrownBy(() -> service.changePlan(STORE, OTHER_ORG, proMonthly.getId(), ACTOR))
                    .isInstanceOf(SubscriptionNotFoundException.class);
            verify(subscriptionGateway, never()).upgradeNow(any(), any(), any());
        }
    }

    // ----------------------------------------------------------------------------------------------- checkout

    @Nested
    @DisplayName("checkout")
    class Checkout {

        @Test
        @DisplayName("checkout opens a session and returns its URL")
        void opensASession() throws Exception {
            inRepository(onBasic());
            when(catalog.requirePurchasablePrice(proMonthly.getId())).thenReturn(proMonthly);
            when(customerGateway.findOrCreate(ORG, null)).thenReturn(new StripeCustomerId("cus_1"));
            when(checkoutGateway.createSubscriptionSession(eq(STORE), eq(ORG), any(), eq(proMonthly), any(), any()))
                    .thenReturn("https://checkout.stripe.test/cs_1");

            CheckoutSessionView session = service.checkout(STORE, ORG, proMonthly.getId(), "https://ok",
                    "https://no");

            assertThat(session.url()).isEqualTo("https://checkout.stripe.test/cs_1");
        }

        @Test
        @DisplayName("a price the catalog never published is refused as not purchasable, not as a Stripe fault")
        void anUnpublishedPriceIsNotPurchasable() throws Exception {
            inRepository(onBasic());
            PlanPriceEntity unpublished = PlanPriceEntity.create(proPlan.getId(), new CurrencyCode("USD"), 3000L,
                    BillingInterval.MONTH, 0);
            when(catalog.requirePurchasablePrice(unpublished.getId())).thenReturn(unpublished);

            // Nothing is wrong with Stripe — the catalog sync has not run. Reporting a provider fault would page
            // somebody about a configuration step.
            assertThatThrownBy(() -> service.checkout(STORE, ORG, unpublished.getId(), "https://ok", "https://no"))
                    .isInstanceOf(PlanPriceNotFoundException.class);
            verify(checkoutGateway, never()).createSubscriptionSession(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("a customer created during checkout is written back to the row")
        void bindsANewCustomer() throws Exception {
            StoreSubscriptionEntity entity = StoreSubscriptionEntity.pending(STORE, ORG);
            inRepository(entity);
            when(catalog.requirePurchasablePrice(proMonthly.getId())).thenReturn(proMonthly);
            when(customerGateway.findOrCreate(ORG, null)).thenReturn(new StripeCustomerId("cus_new"));
            when(checkoutGateway.createSubscriptionSession(any(), any(), any(), any(), any(), any()))
                    .thenReturn("https://checkout.stripe.test/cs_1");

            service.checkout(STORE, ORG, proMonthly.getId(), "https://ok", "https://no");

            assertThat(lastSaved().getStripeCustomerId()).isEqualTo(new StripeCustomerId("cus_new"));
        }

        @Test
        @DisplayName("a customer the row already names is not written again")
        void doesNotRewriteTheSameCustomer() throws Exception {
            inRepository(onBasic());
            when(catalog.requirePurchasablePrice(proMonthly.getId())).thenReturn(proMonthly);
            when(customerGateway.findOrCreate(ORG, null)).thenReturn(new StripeCustomerId("cus_1"));
            when(checkoutGateway.createSubscriptionSession(any(), any(), any(), any(), any(), any()))
                    .thenReturn("https://checkout.stripe.test/cs_1");

            service.checkout(STORE, ORG, proMonthly.getId(), "https://ok", "https://no");

            verify(subscriptions, never()).save(any(StoreSubscriptionEntity.class));
        }
    }

    // -------------------------------------------------------------------------------------------- plan change

    @Nested
    @DisplayName("changePlan")
    class ChangePlan {

        @Test
        @DisplayName("a move to a higher tier is charged and applied now")
        void upgradeAppliesImmediately() throws Exception {
            inRepository(onBasic());
            when(catalog.requirePurchasablePrice(proMonthly.getId())).thenReturn(proMonthly);
            when(catalog.requirePrice(basicMonthly.getId())).thenReturn(basicMonthly);

            service.changePlan(STORE, ORG, proMonthly.getId(), ACTOR);

            verify(subscriptionGateway).upgradeNow(eq(STORE), eq(new StripeSubscriptionId("sub_1")), eq(proMonthly));
            verify(subscriptionGateway, never()).scheduleDowngrade(any(), any(), any(), any());
            assertThat(lastSaved().getPlanPriceId()).isEqualTo(proMonthly.getId());
        }

        @Test
        @DisplayName("Stripe is called before the local row is written, so a refusal changes nothing")
        void providerFirst() throws Exception {
            inRepository(onBasic());
            when(catalog.requirePurchasablePrice(proMonthly.getId())).thenReturn(proMonthly);
            when(catalog.requirePrice(basicMonthly.getId())).thenReturn(basicMonthly);

            service.changePlan(STORE, ORG, proMonthly.getId(), ACTOR);

            InOrder order = inOrder(subscriptionGateway, subscriptions);
            order.verify(subscriptionGateway).upgradeNow(any(), any(), any());
            order.verify(subscriptions).save(any(StoreSubscriptionEntity.class));
        }

        @Test
        @DisplayName("a refused upgrade leaves the local row exactly as it was")
        void aRefusedUpgradeWritesNothing() throws Exception {
            inRepository(onBasic());
            when(catalog.requirePurchasablePrice(proMonthly.getId())).thenReturn(proMonthly);
            when(catalog.requirePrice(basicMonthly.getId())).thenReturn(basicMonthly);
            org.mockito.Mockito.doThrow(BillingProviderUnavailableException.of("stripe", STORE,
                            com.asrevo.cvhome.billing.commons.StripeRequestOperation.SUBSCRIPTION_UPDATE, null, 0,
                            null))
                    .when(subscriptionGateway).upgradeNow(any(), any(), any());

            assertThatThrownBy(() -> service.changePlan(STORE, ORG, proMonthly.getId(), ACTOR))
                    .isInstanceOf(BillingProviderUnavailableException.class);
            verify(subscriptions, never()).save(any(StoreSubscriptionEntity.class));
            verify(audit, never()).record(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("a move to a lower tier is recorded as pending and the plan in force does not move")
        void downgradeIsDeferred() throws Exception {
            StoreSubscriptionEntity entity = StoreSubscriptionEntity.pending(STORE, ORG)
                    .activate(proPlan.getId(), proMonthly.getId(), PERIOD_START, PERIOD_END)
                    .bindProvider(new StripeCustomerId("cus_1"), new StripeSubscriptionId("sub_1"));
            inRepository(entity);
            when(catalog.requirePurchasablePrice(basicMonthly.getId())).thenReturn(basicMonthly);
            when(catalog.requirePrice(proMonthly.getId())).thenReturn(proMonthly);
            when(subscriptionGateway.scheduleDowngrade(STORE, new StripeSubscriptionId("sub_1"), basicMonthly,
                    PERIOD_END)).thenReturn(new StripeScheduleId("sub_sched_1"));

            service.changePlan(STORE, ORG, basicMonthly.getId(), ACTOR);

            StoreSubscriptionEntity saved = lastSaved();
            // Entitlements must not narrow yet: the customer keeps what they paid for until the period ends.
            assertThat(saved.getPlanPriceId()).isEqualTo(proMonthly.getId());
            assertThat(saved.getPendingPlanPriceId()).isEqualTo(basicMonthly.getId());
            assertThat(saved.getPendingEffectiveAt()).isEqualTo(PERIOD_END);
            assertThat(saved.getStripeScheduleId()).isEqualTo(new StripeScheduleId("sub_sched_1"));
            verify(audit).record(eq(SubscriptionStatus.ACTIVE), eq(proPlan.getId()), any(),
                    eq(AuditEventType.PLAN_DOWNGRADE_SCHEDULED), eq(ChangeSource.API), eq(ACTOR));
        }

        @Test
        @DisplayName("moving to the plan already in force does nothing at all")
        void aNoOpChangeTouchesNothing() throws Exception {
            inRepository(onBasic());
            when(catalog.requirePurchasablePrice(basicMonthly.getId())).thenReturn(basicMonthly);
            when(catalog.requirePrice(basicMonthly.getId())).thenReturn(basicMonthly);

            service.changePlan(STORE, ORG, basicMonthly.getId(), ACTOR);

            verify(subscriptionGateway, never()).upgradeNow(any(), any(), any());
            verify(subscriptionGateway, never()).scheduleDowngrade(any(), any(), any(), any());
            verify(subscriptions, never()).save(any(StoreSubscriptionEntity.class));
        }

        @Test
        @DisplayName("within one tier, a dearer interval is an upgrade and a cheaper one a downgrade")
        void priceBreaksTheTieWithinATier() throws Exception {
            PlanPriceEntity basicYearly = PlanPriceEntity.create(basicPlan.getId(), new CurrencyCode("USD"), 10000L,
                    BillingInterval.YEAR, 0).publishedAs(new StripePriceId("price_basic_year"));
            inRepository(onBasic());
            when(catalog.requirePurchasablePrice(basicYearly.getId())).thenReturn(basicYearly);
            when(catalog.requirePrice(basicMonthly.getId())).thenReturn(basicMonthly);

            service.changePlan(STORE, ORG, basicYearly.getId(), ACTOR);

            // Monthly to yearly costs more up front, so it is charged now — the same rule as any other increase,
            // rather than an unanswerable "same tier" case.
            verify(subscriptionGateway).upgradeNow(any(), any(), eq(basicYearly));
        }

        @Test
        @DisplayName("a store that has never bought anything has nothing at Stripe to move")
        void aStoreWithNoProviderSubscriptionIsRefused() throws Exception {
            inRepository(StoreSubscriptionEntity.pending(STORE, ORG));
            when(catalog.requirePurchasablePrice(proMonthly.getId())).thenReturn(proMonthly);

            // Reported as an illegal transition rather than a missing subscription: the row is right there, it is
            // the state that makes the request meaningless.
            assertThatThrownBy(() -> service.changePlan(STORE, ORG, proMonthly.getId(), ACTOR))
                    .isInstanceOf(IllegalSubscriptionTransitionException.class);
        }

        @Test
        @DisplayName("an unpublished target is refused before Stripe is called")
        void anUnpublishedTargetIsRefused() throws Exception {
            inRepository(onBasic());
            PlanPriceEntity unpublished = PlanPriceEntity.create(proPlan.getId(), new CurrencyCode("USD"), 3000L,
                    BillingInterval.MONTH, 0);
            when(catalog.requirePurchasablePrice(unpublished.getId())).thenReturn(unpublished);

            assertThatThrownBy(() -> service.changePlan(STORE, ORG, unpublished.getId(), ACTOR))
                    .isInstanceOf(PlanPriceNotFoundException.class);
            verify(subscriptionGateway, never()).upgradeNow(any(), any(), any());
        }

        @Test
        @DisplayName("the audit row names the plan moved from, read before the entity mutated")
        void auditNamesThePlanMovedFrom() throws Exception {
            inRepository(onBasic());
            when(catalog.requirePurchasablePrice(proMonthly.getId())).thenReturn(proMonthly);
            when(catalog.requirePrice(basicMonthly.getId())).thenReturn(basicMonthly);

            service.changePlan(STORE, ORG, proMonthly.getId(), ACTOR);

            ArgumentCaptor<PlanId> fromPlan = ArgumentCaptor.forClass(PlanId.class);
            verify(audit).record(eq(SubscriptionStatus.ACTIVE), fromPlan.capture(), any(),
                    eq(AuditEventType.PLAN_UPGRADED), eq(ChangeSource.API), eq(ACTOR));
            // The aggregate mutates in place, so reading getPlanId() after the save answers the plan moved *to*.
            // That is how from_plan_id came to be a literal null on every row on the platform.
            assertThat(fromPlan.getValue()).isEqualTo(basicPlan.getId());
        }
    }

    // ------------------------------------------------------------------------------------------------- cancel

    @Nested
    @DisplayName("cancel")
    class Cancel {

        @Test
        @DisplayName("a self-serve cancel switches renewal off and keeps the store running to the period end")
        void scheduledCancel() throws Exception {
            inRepository(onBasic());

            service.cancel(STORE, ORG, false, false, ACTOR);

            verify(subscriptionGateway).setRenewal(STORE, new StripeSubscriptionId("sub_1"), false);
            verify(subscriptionGateway, never()).cancelNow(any(), any());
            StoreSubscriptionEntity saved = lastSaved();
            assertThat(saved.isCancelAtPeriodEnd()).isTrue();
            assertThat(saved.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
            verify(audit).record(any(), any(), any(), eq(AuditEventType.CANCEL_SCHEDULED), eq(ChangeSource.API),
                    eq(ACTOR));
        }

        @Test
        @DisplayName("an immediate cancel is refused to anyone but a platform operator")
        void immediateCancelNeedsSuperAdmin() throws Exception {
            inRepository(onBasic());

            // Taking away something already bought is not a thing a customer should be able to do to themselves by
            // accident.
            assertThatThrownBy(() -> service.cancel(STORE, ORG, true, false, ACTOR))
                    .isInstanceOf(ImmediateCancelForbiddenException.class);
            verify(subscriptionGateway, never()).cancelNow(any(), any());
        }

        @Test
        @DisplayName("a platform operator may end it now")
        void immediateCancelBySuperAdmin() throws Exception {
            inRepository(onBasic());

            service.cancel(STORE, ORG, true, true, ACTOR);

            verify(subscriptionGateway).cancelNow(STORE, new StripeSubscriptionId("sub_1"));
            assertThat(lastSaved().getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
            verify(audit).record(any(), any(), any(), eq(AuditEventType.CANCELED), eq(ChangeSource.API), eq(ACTOR));
        }

        @Test
        @DisplayName("a pending schedule is released before renewal is switched off")
        void releasesTheScheduleFirst() throws Exception {
            StoreSubscriptionEntity entity = onBasic();
            entity.scheduleDowngradeTo(proMonthly.getId(), PERIOD_END).bindSchedule(new StripeScheduleId("ss_1"));
            inRepository(entity);

            service.cancel(STORE, ORG, false, false, ACTOR);

            // Stripe refuses to set cancellation behaviour directly on a scheduled subscription and says to change
            // the schedule instead, so the release has to come first.
            InOrder order = inOrder(subscriptionGateway);
            order.verify(subscriptionGateway).releaseSchedule(STORE, new StripeScheduleId("ss_1"));
            order.verify(subscriptionGateway).setRenewal(STORE, new StripeSubscriptionId("sub_1"), false);
        }

        @Test
        @DisplayName("a store with nothing at Stripe cannot be cancelled")
        void cancelWithoutAProviderSubscription() throws Exception {
            inRepository(StoreSubscriptionEntity.pending(STORE, ORG));

            assertThatThrownBy(() -> service.cancel(STORE, ORG, false, false, ACTOR))
                    .isInstanceOf(IllegalSubscriptionTransitionException.class);
        }
    }

    // ------------------------------------------------------------------------------------------------- resume

    @Nested
    @DisplayName("resume")
    class Resume {

        @Test
        @DisplayName("resuming switches renewal back on and clears the flag")
        void resumeClearsTheFlag() throws Exception {
            StoreSubscriptionEntity entity = onBasic();
            entity.scheduleCancel();
            inRepository(entity);

            service.resume(STORE, ORG, ACTOR);

            verify(subscriptionGateway).setRenewal(STORE, new StripeSubscriptionId("sub_1"), true);
            assertThat(lastSaved().isCancelAtPeriodEnd()).isFalse();
            verify(audit).record(any(), any(), any(), eq(AuditEventType.CANCEL_REVOKED), eq(ChangeSource.API),
                    eq(ACTOR));
        }

        @Test
        @DisplayName("there is nothing to resume when nothing was scheduled, and Stripe is never touched")
        void nothingToResume() throws Exception {
            inRepository(onBasic());

            // Checked before anything is sent. An earlier version released the schedule first and only then found
            // there was nothing to resume, so the request failed with Stripe already changed.
            assertThatThrownBy(() -> service.resume(STORE, ORG, ACTOR))
                    .isInstanceOf(IllegalSubscriptionTransitionException.class);
            verify(subscriptionGateway, never()).releaseSchedule(any(), any());
            verify(subscriptionGateway, never()).setRenewal(any(), any(), anyBoolean());
        }

        @Test
        @DisplayName("a pending downgrade alone is enough to resume, and it is called off on both sides")
        void aPendingDowngradeIsCalledOff() throws Exception {
            StoreSubscriptionEntity entity = onBasic();
            entity.scheduleDowngradeTo(proMonthly.getId(), PERIOD_END).bindSchedule(new StripeScheduleId("ss_1"));
            inRepository(entity);

            service.resume(STORE, ORG, ACTOR);

            // Two regressions in one case. revokeScheduledCancel refuses a subscription that was never cancelled,
            // and this store was not — so resuming it released the schedule and set renewal at Stripe and *then*
            // threw, leaving the provider changed and the row untouched. And the local pending change was never
            // cleared, so the customer went on being shown a downgrade they had just undone.
            verify(subscriptionGateway).releaseSchedule(STORE, new StripeScheduleId("ss_1"));
            StoreSubscriptionEntity saved = lastSaved();
            assertThat(saved.getPendingPlanPriceId()).isNull();
            assertThat(saved.getPendingEffectiveAt()).isNull();
            assertThat(saved.getStripeScheduleId()).isNull();
        }

        @Test
        @DisplayName("resuming a cancelled subscription with a pending downgrade drops both")
        void bothAreRevokedTogether() throws Exception {
            StoreSubscriptionEntity entity = onBasic();
            entity.scheduleDowngradeTo(proMonthly.getId(), PERIOD_END).bindSchedule(new StripeScheduleId("ss_1"));
            // Set last, because scheduleCancel drops a pending change of its own accord; re-added afterwards so the
            // row carries both, which is the state a webhook reconciliation can leave behind.
            entity.scheduleCancel();
            entity.scheduleDowngradeTo(proMonthly.getId(), PERIOD_END).bindSchedule(new StripeScheduleId("ss_1"));
            inRepository(entity);

            service.resume(STORE, ORG, ACTOR);

            StoreSubscriptionEntity saved = lastSaved();
            assertThat(saved.isCancelAtPeriodEnd()).isFalse();
            // Without clearing this, ApplyPendingPlanChangesJob would later move a paying store onto the cheaper
            // plan with no schedule at Stripe to have caused it.
            assertThat(saved.getPendingPlanPriceId()).isNull();
        }
    }

    // ------------------------------------------------------------------------------------------ job-driven work

    @Nested
    @DisplayName("job-driven changes")
    class JobDriven {

        @Test
        @DisplayName("a deferred change that came due is applied and attributed to the platform")
        void applyPendingChange() throws Exception {
            StoreSubscriptionEntity entity = onBasic();
            entity.scheduleDowngradeTo(proMonthly.getId(), PERIOD_END);
            inRepository(entity);
            when(catalog.requirePrice(proMonthly.getId())).thenReturn(proMonthly);

            service.applyPendingChange(STORE);

            assertThat(lastSaved().getPlanPriceId()).isEqualTo(proMonthly.getId());
            // Named as a job rather than a person, so the trail distinguishes "the platform did this" from
            // "someone did this".
            verify(audit).record(eq(SubscriptionStatus.ACTIVE), eq(basicPlan.getId()), any(),
                    eq(AuditEventType.PLAN_DOWNGRADE_APPLIED), eq(ChangeSource.JOB), eq("billing-job"));
        }

        @Test
        @DisplayName("nothing pending means the webhook got there first, which is the normal case")
        void applyPendingChangeWithNothingPending() throws Exception {
            inRepository(onBasic());

            service.applyPendingChange(STORE);

            verify(subscriptions, never()).save(any(StoreSubscriptionEntity.class));
            verify(audit, never()).record(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("an expired trial is suspended")
        void expireTrial() throws Exception {
            inRepository(StoreSubscriptionEntity.trialing(STORE, ORG, basicPlan.getId(), basicMonthly.getId(),
                    PERIOD_END));

            service.expireTrial(STORE);

            assertThat(lastSaved().getStatus()).isEqualTo(SubscriptionStatus.SUSPENDED);
            verify(audit).record(eq(SubscriptionStatus.TRIALING), any(), any(), eq(AuditEventType.SUSPENDED),
                    eq(ChangeSource.JOB), eq("billing-job"));
        }

        @Test
        @DisplayName("a store that paid between the job noticing and the command running is left alone")
        void expireTrialOnAPayingStore() throws Exception {
            inRepository(onBasic());

            service.expireTrial(STORE);

            // Not an error: the job runs on a schedule and the world moves underneath it.
            verify(subscriptions, never()).save(any(StoreSubscriptionEntity.class));
        }

        @Test
        @DisplayName("a store whose grace window closed is suspended")
        void suspendUnpaid() throws Exception {
            StoreSubscriptionEntity entity = onBasic();
            entity.markPastDue(PERIOD_END);
            inRepository(entity);

            service.suspendUnpaid(STORE);

            assertThat(lastSaved().getStatus()).isEqualTo(SubscriptionStatus.SUSPENDED);
        }

        @Test
        @DisplayName("a store that is no longer past due is left alone")
        void suspendUnpaidOnAPayingStore() throws Exception {
            inRepository(onBasic());

            service.suspendUnpaid(STORE);

            verify(subscriptions, never()).save(any(StoreSubscriptionEntity.class));
        }

        @Test
        @DisplayName("a store that has vanished is reported rather than silently skipped")
        void aMissingStoreIsReported() {
            when(subscriptions.findById(STORE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.expireTrial(STORE))
                    .isInstanceOf(SubscriptionNotFoundException.class);
        }
    }

    // ---------------------------------------------------------------------------------------------- snapshot

    @Test
    @DisplayName("the entitlement snapshot is read unscoped, because the pods that ask span orgs")
    void snapshotIsUnscoped() throws Exception {
        inRepository(onBasic());

        service.snapshot(STORE);

        verify(subscriptions).findById(STORE);
        verify(mappers).toSnapshot(any(StoreSubscriptionEntity.class));
    }

}
