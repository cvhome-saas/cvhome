package com.asrevo.cvhome.billing.domain;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.asrevo.cvhome.billing.commons.PlanId;
import com.asrevo.cvhome.billing.commons.PlanPriceId;
import com.asrevo.cvhome.billing.commons.StripeCustomerId;
import com.asrevo.cvhome.billing.commons.StripeScheduleId;
import com.asrevo.cvhome.billing.commons.StripeSubscriptionId;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.commons.errors.IllegalSubscriptionTransitionException;
import com.asrevo.cvhome.billing.events.SubscriptionActivatedEvent;
import com.asrevo.cvhome.billing.events.SubscriptionCanceledEvent;
import com.asrevo.cvhome.billing.events.SubscriptionPastDueEvent;
import com.asrevo.cvhome.billing.events.SubscriptionPlanChangedEvent;
import com.asrevo.cvhome.billing.events.SubscriptionRenewedEvent;
import com.asrevo.cvhome.billing.events.SubscriptionSuspendedEvent;
import com.asrevo.cvhome.billing.events.SubscriptionTrialStartedEvent;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The subscription state machine, every legal move and every refused one.
 *
 * <p>
 * This aggregate is the only place a subscription's status may change, and every mutator is expected to do three
 * things together: check the move is legal, apply it, and register the event. A test that only asserted the status
 * would pass for a method that silently stopped emitting its event — which is what the entitlement caches and the
 * audit trail downstream are reading — so each case checks the event stream too.
 * </p>
 *
 * <p>
 * The illegal cases matter more than the legal ones. {@code SubscriptionStatus.canTransitionTo} is the table, and
 * without a test that walks it, a state added to the enum without a row in {@code LEGAL} would silently permit
 * <em>nothing</em> — {@code getOrDefault(this, Set.of())} — rather than fail loudly.
 * </p>
 */
class StoreSubscriptionEntityTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final ManagerOrgId ORG = new ManagerOrgId("32a034a43cd77581d105c87a");

    private static final PlanId PLAN = PlanId.newId();

    private static final PlanId OTHER_PLAN = PlanId.newId();

    private static final PlanPriceId PRICE = PlanPriceId.newId();

    private static final PlanPriceId CHEAPER_PRICE = PlanPriceId.newId();

    private static final Instant PERIOD_START = Instant.parse("2026-01-01T00:00:00Z");

    private static final Instant PERIOD_END = Instant.parse("2026-02-01T00:00:00Z");

    // ----------------------------------------------------------------------------------------------- helpers

    /**
     * The events an aggregate is holding. {@code AbstractAggregateRoot.domainEvents()} is protected, and the
     * alternative — asserting through a repository save in a Spring context — would test Spring rather than this.
     */
    @SuppressWarnings("unchecked")
    private static List<Object> eventsOf(StoreSubscriptionEntity entity) {
        try {
            Method method = findDomainEvents(entity.getClass());
            method.setAccessible(true);
            return List.copyOf((Collection<Object>) method.invoke(entity));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Method findDomainEvents(Class<?> type) throws NoSuchMethodException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredMethod("domainEvents");
            } catch (NoSuchMethodException ignored) {
                // keep walking up to AbstractAggregateRoot
            }
        }
        throw new NoSuchMethodException("domainEvents");
    }

    private static StoreSubscriptionEntity active() throws IllegalSubscriptionTransitionException {
        return StoreSubscriptionEntity.pending(STORE, ORG).activate(PLAN, PRICE, PERIOD_START, PERIOD_END);
    }

    private static StoreSubscriptionEntity trialing() {
        return StoreSubscriptionEntity.trialing(STORE, ORG, PLAN, PRICE, PERIOD_END);
    }

    // ----------------------------------------------------------------------------------------------- creation

    @Nested
    @DisplayName("creation")
    class Creation {

        @Test
        @DisplayName("a store whose org has spent its trial starts unpaid and announces nothing")
        void pendingStartsUnpaid() {
            StoreSubscriptionEntity entity = StoreSubscriptionEntity.pending(STORE, ORG);

            assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.PENDING);
            assertThat(entity.getId()).isEqualTo(STORE);
            assertThat(entity.getOrgId()).isEqualTo(ORG);
            assertThat(entity.getPlanId()).isNull();
            assertThat(entity.isCancelAtPeriodEnd()).isFalse();
            assertThat(entity.operable()).isFalse();
            // Nothing has been granted, so nothing is announced. A PENDING store that emitted an activation would
            // have every pod's entitlement cache treat it as paid.
            assertThat(eventsOf(entity)).isEmpty();
        }

        @Test
        @DisplayName("a trial opens a period ending when the trial does, and says so")
        void trialingOpensAPeriod() {
            StoreSubscriptionEntity entity = trialing();

            assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.TRIALING);
            assertThat(entity.getPlanId()).isEqualTo(PLAN);
            assertThat(entity.getPlanPriceId()).isEqualTo(PRICE);
            assertThat(entity.getTrialEnd()).isEqualTo(PERIOD_END);
            // The period is the trial: a trial that reported no renewal date would render as "renews never".
            assertThat(entity.getCurrentPeriodEnd()).isEqualTo(PERIOD_END);
            assertThat(entity.getCurrentPeriodStart()).isEqualTo(entity.getCreatedDate());
            assertThat(entity.operable()).isTrue();
            assertThat(eventsOf(entity)).singleElement().isInstanceOf(SubscriptionTrialStartedEvent.class);
        }
    }

    // ----------------------------------------------------------------------------------------------- activate

    @Nested
    @DisplayName("activate")
    class Activate {

        @Test
        @DisplayName("the first payment opens the paid period and clears the trial")
        void activateFromPending() throws Exception {
            StoreSubscriptionEntity entity = StoreSubscriptionEntity.pending(STORE, ORG)
                    .activate(PLAN, PRICE, PERIOD_START, PERIOD_END);

            assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
            assertThat(entity.getPlanId()).isEqualTo(PLAN);
            assertThat(entity.getPlanPriceId()).isEqualTo(PRICE);
            assertThat(entity.getCurrentPeriodStart()).isEqualTo(PERIOD_START);
            assertThat(entity.getCurrentPeriodEnd()).isEqualTo(PERIOD_END);
            assertThat(entity.getTrialEnd()).isNull();
            assertThat(eventsOf(entity)).singleElement().isInstanceOf(SubscriptionActivatedEvent.class);
        }

        @Test
        @DisplayName("a suspended store that pays comes back and loses its suspension marks")
        void activateFromSuspendedClearsMarks() throws Exception {
            StoreSubscriptionEntity entity = active();
            entity.markPastDue(PERIOD_END);
            entity.suspend(PERIOD_END);

            entity.activate(PLAN, PRICE, PERIOD_START, PERIOD_END);

            assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
            // Both have to go, or the store reads as suspended-and-active at once and the health screen shows a
            // grace window that ended.
            assertThat(entity.getSuspendedAt()).isNull();
            assertThat(entity.getGraceUntil()).isNull();
        }

        @Test
        @DisplayName("activating an already active subscription is a renewal, not a second activation")
        void activateWhenActiveRenews() throws Exception {
            StoreSubscriptionEntity entity = active();
            Instant nextEnd = PERIOD_END.plus(30, ChronoUnit.DAYS);

            entity.activate(PLAN, PRICE, PERIOD_END, nextEnd);

            assertThat(entity.getCurrentPeriodEnd()).isEqualTo(nextEnd);
            // Stripe sends invoice.payment_succeeded for the renewal too. Announcing a second activation would have
            // every listener treat a monthly renewal as a brand-new subscription.
            assertThat(eventsOf(entity)).hasSize(2)
                    .last()
                    .isInstanceOf(SubscriptionRenewedEvent.class);
        }

        @Test
        @DisplayName("a cancelled subscription cannot be activated")
        void activateFromCanceledIsRefused() throws Exception {
            StoreSubscriptionEntity entity = active();
            entity.cancelNow(PERIOD_END);

            assertThatThrownBy(() -> entity.activate(PLAN, PRICE, PERIOD_START, PERIOD_END))
                    .isInstanceOf(IllegalSubscriptionTransitionException.class);
            assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
        }

        @Test
        @DisplayName("a trial that converts becomes active")
        void activateFromTrialing() throws Exception {
            StoreSubscriptionEntity entity = trialing();

            entity.activate(PLAN, PRICE, PERIOD_START, PERIOD_END);

            assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
            assertThat(entity.getTrialEnd()).isNull();
        }
    }

    // ----------------------------------------------------------------------------------------------- renew

    @Nested
    @DisplayName("renew")
    class Renew {

        @Test
        @DisplayName("a renewal rolls the period forward and clears any grace window")
        void renewClearsGrace() throws Exception {
            StoreSubscriptionEntity entity = active();
            entity.markPastDue(PERIOD_END);
            Instant nextEnd = PERIOD_END.plus(30, ChronoUnit.DAYS);

            entity.renew(PERIOD_END, nextEnd);

            assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
            assertThat(entity.getCurrentPeriodStart()).isEqualTo(PERIOD_END);
            assertThat(entity.getCurrentPeriodEnd()).isEqualTo(nextEnd);
            // A past-due store that pays is no longer in a grace window; leaving the date would suspend it later.
            assertThat(entity.getGraceUntil()).isNull();
        }
    }

    // ----------------------------------------------------------------------------------------------- past due

    @Nested
    @DisplayName("markPastDue")
    class MarkPastDue {

        @Test
        @DisplayName("a failed renewal opens a grace window and leaves the store usable")
        void pastDueStaysOperable() throws Exception {
            StoreSubscriptionEntity entity = active();
            Instant grace = PERIOD_END.plus(7, ChronoUnit.DAYS);

            entity.markPastDue(grace);

            assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.PAST_DUE);
            assertThat(entity.getGraceUntil()).isEqualTo(grace);
            // The whole point of PAST_DUE: a merchant who cannot trade cannot earn the money to settle the invoice.
            assertThat(entity.operable()).isTrue();
            assertThat(eventsOf(entity)).last().isInstanceOf(SubscriptionPastDueEvent.class);
        }

        @Test
        @DisplayName("a redelivered failure changes nothing and announces nothing twice")
        void pastDueTwiceIsANoOp() throws Exception {
            StoreSubscriptionEntity entity = active();
            entity.markPastDue(PERIOD_END);
            int announced = eventsOf(entity).size();

            entity.markPastDue(PERIOD_END.plus(30, ChronoUnit.DAYS));

            // Idempotent to the letter: not even the grace window moves, because the second delivery is the same
            // event, not a second failure.
            assertThat(entity.getGraceUntil()).isEqualTo(PERIOD_END);
            assertThat(eventsOf(entity)).hasSize(announced);
        }

        @Test
        @DisplayName("an unpaid store cannot go past due — it was never paying")
        void pastDueFromPendingIsRefused() {
            StoreSubscriptionEntity entity = StoreSubscriptionEntity.pending(STORE, ORG);

            assertThatThrownBy(() -> entity.markPastDue(PERIOD_END))
                    .isInstanceOf(IllegalSubscriptionTransitionException.class);
        }
    }

    // ----------------------------------------------------------------------------------------------- suspend

    @Nested
    @DisplayName("suspend")
    class Suspend {

        @Test
        @DisplayName("a closed grace window ends access")
        void suspendFromPastDue() throws Exception {
            StoreSubscriptionEntity entity = active();
            entity.markPastDue(PERIOD_END);

            entity.suspend(PERIOD_END);

            assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.SUSPENDED);
            assertThat(entity.getSuspendedAt()).isEqualTo(PERIOD_END);
            assertThat(entity.operable()).isFalse();
            assertThat(eventsOf(entity)).last().isInstanceOf(SubscriptionSuspendedEvent.class);
        }

        @Test
        @DisplayName("a trial that ran out ends access")
        void suspendFromTrialing() throws Exception {
            StoreSubscriptionEntity entity = trialing();

            entity.suspend(PERIOD_END);

            assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.SUSPENDED);
        }

        @Test
        @DisplayName("suspending twice is a no-op")
        void suspendTwiceIsANoOp() throws Exception {
            StoreSubscriptionEntity entity = trialing();
            entity.suspend(PERIOD_END);
            int announced = eventsOf(entity).size();

            entity.suspend(PERIOD_END.plus(1, ChronoUnit.DAYS));

            assertThat(entity.getSuspendedAt()).isEqualTo(PERIOD_END);
            assertThat(eventsOf(entity)).hasSize(announced);
        }

        @Test
        @DisplayName("an unpaid store cannot be suspended: PENDING is already the gate")
        void suspendFromPendingIsRefused() {
            StoreSubscriptionEntity entity = StoreSubscriptionEntity.pending(STORE, ORG);

            assertThatThrownBy(() -> entity.suspend(PERIOD_END))
                    .isInstanceOf(IllegalSubscriptionTransitionException.class);
        }

        @Test
        @DisplayName("a cancelled subscription cannot be suspended")
        void suspendFromCanceledIsRefused() throws Exception {
            StoreSubscriptionEntity entity = active();
            entity.cancelNow(PERIOD_END);

            assertThatThrownBy(() -> entity.suspend(PERIOD_END))
                    .isInstanceOf(IllegalSubscriptionTransitionException.class);
        }
    }

    // ----------------------------------------------------------------------------------------------- plan moves

    @Nested
    @DisplayName("plan changes")
    class PlanChanges {

        @Test
        @DisplayName("an upgrade lands now, drops anything pending and announces the change")
        void upgradeAppliesImmediately() throws Exception {
            StoreSubscriptionEntity entity = active();
            entity.scheduleDowngradeTo(CHEAPER_PRICE, PERIOD_END);
            entity.bindSchedule(new StripeScheduleId("sub_sched_1"));

            entity.upgradeTo(OTHER_PLAN, CHEAPER_PRICE, PERIOD_START, PERIOD_END);

            assertThat(entity.getPlanId()).isEqualTo(OTHER_PLAN);
            assertThat(entity.getPlanPriceId()).isEqualTo(CHEAPER_PRICE);
            // The deferred drop is gone with its provider schedule: it was a move from a plan the store is no
            // longer on, and leaving it would drop the customer off the plan they just bought.
            assertThat(entity.getPendingPlanPriceId()).isNull();
            assertThat(entity.getPendingEffectiveAt()).isNull();
            assertThat(entity.getStripeScheduleId()).isNull();
            assertThat(eventsOf(entity)).last().isInstanceOf(SubscriptionPlanChangedEvent.class);
        }

        @Test
        @DisplayName("a downgrade only records the intent — nothing narrows yet, and nothing is announced")
        void downgradeIsRecordedNotApplied() throws Exception {
            StoreSubscriptionEntity entity = active();
            int announced = eventsOf(entity).size();

            entity.scheduleDowngradeTo(CHEAPER_PRICE, PERIOD_END);

            assertThat(entity.getPendingPlanPriceId()).isEqualTo(CHEAPER_PRICE);
            assertThat(entity.getPendingEffectiveAt()).isEqualTo(PERIOD_END);
            // The customer keeps what they paid for, so the plan in force must not move.
            assertThat(entity.getPlanPriceId()).isEqualTo(PRICE);
            assertThat(entity.getPlanId()).isEqualTo(PLAN);
            // Nothing has changed for anything reading entitlements, so nothing is told.
            assertThat(eventsOf(entity)).hasSize(announced);
        }

        @Test
        @DisplayName("a deferred change lands once and only once")
        void applyPendingChangeIsIdempotent() throws Exception {
            StoreSubscriptionEntity entity = active();
            entity.scheduleDowngradeTo(CHEAPER_PRICE, PERIOD_END);
            entity.bindSchedule(new StripeScheduleId("sub_sched_1"));

            entity.applyPendingChange(OTHER_PLAN, CHEAPER_PRICE);
            int announced = eventsOf(entity).size();

            // The webhook and the safety-net job both land here; the second must do nothing at all.
            entity.applyPendingChange(PLAN, PRICE);

            assertThat(entity.getPlanId()).isEqualTo(OTHER_PLAN);
            assertThat(entity.getPlanPriceId()).isEqualTo(CHEAPER_PRICE);
            assertThat(entity.getStripeScheduleId()).isNull();
            assertThat(eventsOf(entity)).hasSize(announced);
        }

        @Test
        @DisplayName("applying with nothing pending changes nothing")
        void applyPendingChangeWithNothingPending() throws Exception {
            StoreSubscriptionEntity entity = active();
            int announced = eventsOf(entity).size();

            entity.applyPendingChange(OTHER_PLAN, CHEAPER_PRICE);

            assertThat(entity.getPlanId()).isEqualTo(PLAN);
            assertThat(eventsOf(entity)).hasSize(announced);
        }
    }

    // ----------------------------------------------------------------------------------------------- renewal flag

    @Nested
    @DisplayName("renewal")
    class Renewal {

        @Test
        @DisplayName("switching renewal off keeps the status and drops any pending downgrade")
        void scheduleCancelKeepsStatus() throws Exception {
            StoreSubscriptionEntity entity = active();
            entity.scheduleDowngradeTo(CHEAPER_PRICE, PERIOD_END);

            entity.scheduleCancel();

            assertThat(entity.isCancelAtPeriodEnd()).isTrue();
            // Not a cancellation: the customer keeps everything they paid for until the period runs out.
            assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
            // The downgrade would have taken effect at the very moment the subscription ends, so it cannot happen.
            assertThat(entity.getPendingPlanPriceId()).isNull();
        }

        @Test
        @DisplayName("resuming clears the flag")
        void revokeScheduledCancel() throws Exception {
            StoreSubscriptionEntity entity = active();
            entity.scheduleCancel();

            entity.revokeScheduledCancel();

            assertThat(entity.isCancelAtPeriodEnd()).isFalse();
        }

        @Test
        @DisplayName("resuming a subscription that was never cancelled is refused")
        void revokeWithoutCancelIsRefused() throws Exception {
            StoreSubscriptionEntity entity = active();

            assertThatThrownBy(entity::revokeScheduledCancel)
                    .isInstanceOf(IllegalSubscriptionTransitionException.class);
        }

        @Test
        @DisplayName("calling off a pending change drops it and its provider schedule, and nothing else")
        void revokePendingChange() throws Exception {
            StoreSubscriptionEntity entity = active();
            entity.scheduleCancel();
            entity.scheduleDowngradeTo(CHEAPER_PRICE, PERIOD_END);
            entity.bindSchedule(new StripeScheduleId("sub_sched_1"));

            entity.revokePendingChange();

            assertThat(entity.getPendingPlanPriceId()).isNull();
            assertThat(entity.getPendingEffectiveAt()).isNull();
            assertThat(entity.getStripeScheduleId()).isNull();
            // Independently revocable: dropping a scheduled downgrade says nothing about whether the subscription
            // renews, and asserting one while revoking the other is what made resume throw mid-flight.
            assertThat(entity.isCancelAtPeriodEnd()).isTrue();
            assertThat(entity.getPlanPriceId()).isEqualTo(PRICE);
        }

        @Test
        @DisplayName("calling off a pending change when there is none is a no-op")
        void revokePendingChangeWithNothingPending() throws Exception {
            StoreSubscriptionEntity entity = active();
            Instant before = entity.getUpdatedDate();

            entity.revokePendingChange();

            assertThat(entity.getUpdatedDate()).isEqualTo(before);
        }

        @Test
        @DisplayName("the provider can turn the flag on, and back off again")
        void reconcileRenewalMovesBothWays() throws Exception {
            StoreSubscriptionEntity entity = active();

            entity.reconcileRenewal(true);
            assertThat(entity.isCancelAtPeriodEnd()).isTrue();

            // The half that was missing: a late webhook from a cancel arriving after a resume used to leave the
            // subscription reading "will not renew" for good.
            entity.reconcileRenewal(false);
            assertThat(entity.isCancelAtPeriodEnd()).isFalse();
        }

        @Test
        @DisplayName("reconciling to the flag already held changes nothing")
        void reconcileRenewalNoOp() throws Exception {
            StoreSubscriptionEntity entity = active();
            entity.scheduleDowngradeTo(CHEAPER_PRICE, PERIOD_END);
            Instant before = entity.getUpdatedDate();

            entity.reconcileRenewal(false);

            // Specifically: the pending change survives, because nothing happened.
            assertThat(entity.getPendingPlanPriceId()).isEqualTo(CHEAPER_PRICE);
            assertThat(entity.getUpdatedDate()).isEqualTo(before);
        }

        @Test
        @DisplayName("the provider switching renewal off also drops a pending downgrade")
        void reconcileRenewalOnClearsPending() throws Exception {
            StoreSubscriptionEntity entity = active();
            entity.scheduleDowngradeTo(CHEAPER_PRICE, PERIOD_END);

            entity.reconcileRenewal(true);

            assertThat(entity.getPendingPlanPriceId()).isNull();
        }
    }

    // ----------------------------------------------------------------------------------------------- end of life

    @Nested
    @DisplayName("cancel and reopen")
    class CancelAndReopen {

        @Test
        @DisplayName("cancelling ends it, clears the renewal flag and anything pending")
        void cancelNow() throws Exception {
            StoreSubscriptionEntity entity = active();
            entity.scheduleCancel();
            entity.scheduleDowngradeTo(CHEAPER_PRICE, PERIOD_END);

            entity.cancelNow(PERIOD_END);

            assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
            assertThat(entity.getCanceledAt()).isEqualTo(PERIOD_END);
            assertThat(entity.isCancelAtPeriodEnd()).isFalse();
            assertThat(entity.getPendingPlanPriceId()).isNull();
            assertThat(entity.operable()).isFalse();
            assertThat(eventsOf(entity)).last().isInstanceOf(SubscriptionCanceledEvent.class);
        }

        @Test
        @DisplayName("cancelling twice is a no-op — Stripe sends the deletion more than once")
        void cancelTwiceIsANoOp() throws Exception {
            StoreSubscriptionEntity entity = active();
            entity.cancelNow(PERIOD_END);
            int announced = eventsOf(entity).size();

            entity.cancelNow(PERIOD_END.plus(1, ChronoUnit.DAYS));

            assertThat(entity.getCanceledAt()).isEqualTo(PERIOD_END);
            assertThat(eventsOf(entity)).hasSize(announced);
        }

        @Test
        @DisplayName("an unpaid store can be cancelled outright")
        void cancelFromPending() throws Exception {
            StoreSubscriptionEntity entity = StoreSubscriptionEntity.pending(STORE, ORG);

            entity.cancelNow(PERIOD_END);

            assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
        }

        @Test
        @DisplayName("reopening returns a cancelled store to unpaid and forgets every provider identifier")
        void reopenClearsProviderState() throws Exception {
            StoreSubscriptionEntity entity = active();
            entity.bindProvider(new StripeCustomerId("cus_1"), new StripeSubscriptionId("sub_1"));
            entity.bindSchedule(new StripeScheduleId("sub_sched_1"));
            entity.cancelNow(PERIOD_END);

            entity.reopen();

            assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.PENDING);
            // The subscription and schedule are gone at Stripe; keeping their ids would have the next change act on
            // an object that no longer exists.
            assertThat(entity.getStripeSubscriptionId()).isNull();
            assertThat(entity.getStripeScheduleId()).isNull();
            assertThat(entity.getCanceledAt()).isNull();
            assertThat(entity.getSuspendedAt()).isNull();
            assertThat(entity.getGraceUntil()).isNull();
            assertThat(entity.getCurrentPeriodStart()).isNull();
            assertThat(entity.getCurrentPeriodEnd()).isNull();
            // The customer is the org's and outlives the subscription — the card stays on file.
            assertThat(entity.getStripeCustomerId()).isEqualTo(new StripeCustomerId("cus_1"));
        }

        @Test
        @DisplayName("only a cancelled subscription may be reopened")
        void reopenFromActiveIsRefused() throws Exception {
            StoreSubscriptionEntity entity = active();

            assertThatThrownBy(entity::reopen).isInstanceOf(IllegalSubscriptionTransitionException.class);
        }
    }

    // ----------------------------------------------------------------------------------------------- binding

    @Nested
    @DisplayName("provider identifiers")
    class ProviderIdentifiers {

        @Test
        @DisplayName("binding the provider records both ids without moving the status")
        void bindProviderIsNotATransition() {
            StoreSubscriptionEntity entity = StoreSubscriptionEntity.pending(STORE, ORG);

            entity.bindProvider(new StripeCustomerId("cus_1"), new StripeSubscriptionId("sub_1"));

            assertThat(entity.getStripeCustomerId()).isEqualTo(new StripeCustomerId("cus_1"));
            assertThat(entity.getStripeSubscriptionId()).isEqualTo(new StripeSubscriptionId("sub_1"));
            // Checkout completing is not payment. Activating here would open stores that abandoned the payment page.
            assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.PENDING);
            assertThat(eventsOf(entity)).isEmpty();
        }

        @Test
        @DisplayName("binding only the customer leaves the subscription unbound")
        void bindCustomerAlone() {
            StoreSubscriptionEntity entity = StoreSubscriptionEntity.pending(STORE, ORG);

            entity.bindCustomer(new StripeCustomerId("cus_1"));

            assertThat(entity.getStripeCustomerId()).isEqualTo(new StripeCustomerId("cus_1"));
            assertThat(entity.getStripeSubscriptionId()).isNull();
        }
    }

    // ----------------------------------------------------------------------------------------------- the table

    @Nested
    @DisplayName("the transition table")
    class TransitionTable {

        @ParameterizedTest
        @EnumSource(SubscriptionStatus.class)
        @DisplayName("every state may stay where it is, so a redelivered webhook is never a failure")
        void staysPutIsAlwaysLegal(SubscriptionStatus status) {
            assertThat(status.canTransitionTo(status)).isTrue();
        }

        @ParameterizedTest
        @EnumSource(SubscriptionStatus.class)
        @DisplayName("every state can reach at least one other, so no state is a dead end by omission")
        void noStateIsStranded(SubscriptionStatus status) {
            long reachable = java.util.Arrays.stream(SubscriptionStatus.values())
                    .filter(target -> target != status)
                    .filter(status::canTransitionTo)
                    .count();

            assertThat(reachable)
                    .as("%s reaches nothing — a state with no row in LEGAL silently permits nothing", status)
                    .isPositive();
        }

        @Test
        @DisplayName("only trialling, active and past due may be worked in")
        void operableStates() {
            assertThat(java.util.Arrays.stream(SubscriptionStatus.values())
                    .filter(SubscriptionStatus::operable)
                    .toList())
                    .containsExactlyInAnyOrder(SubscriptionStatus.TRIALING, SubscriptionStatus.ACTIVE,
                            SubscriptionStatus.PAST_DUE);
        }

        @Test
        @DisplayName("a store cannot go straight from unpaid to past due or suspended")
        void pendingIsNarrow() {
            assertThat(SubscriptionStatus.PENDING.canTransitionTo(SubscriptionStatus.PAST_DUE)).isFalse();
            assertThat(SubscriptionStatus.PENDING.canTransitionTo(SubscriptionStatus.SUSPENDED)).isFalse();
            assertThat(SubscriptionStatus.PENDING.canTransitionTo(SubscriptionStatus.TRIALING)).isTrue();
            assertThat(SubscriptionStatus.PENDING.canTransitionTo(SubscriptionStatus.ACTIVE)).isTrue();
        }

        @Test
        @DisplayName("a trial cannot fail a payment it never made")
        void trialingCannotGoPastDue() {
            assertThat(SubscriptionStatus.TRIALING.canTransitionTo(SubscriptionStatus.PAST_DUE)).isFalse();
        }

        @Test
        @DisplayName("cancelled leads only back to unpaid")
        void canceledOnlyReopens() {
            assertThat(SubscriptionStatus.CANCELED.canTransitionTo(SubscriptionStatus.PENDING)).isTrue();
            assertThat(SubscriptionStatus.CANCELED.canTransitionTo(SubscriptionStatus.ACTIVE)).isFalse();
            assertThat(SubscriptionStatus.CANCELED.canTransitionTo(SubscriptionStatus.TRIALING)).isFalse();
            assertThat(SubscriptionStatus.CANCELED.canTransitionTo(SubscriptionStatus.SUSPENDED)).isFalse();
        }

        @Test
        @DisplayName("a suspended store comes back by paying, never by trialling again")
        void suspendedComesBackByPaying() {
            assertThat(SubscriptionStatus.SUSPENDED.canTransitionTo(SubscriptionStatus.ACTIVE)).isTrue();
            assertThat(SubscriptionStatus.SUSPENDED.canTransitionTo(SubscriptionStatus.TRIALING)).isFalse();
            assertThat(SubscriptionStatus.SUSPENDED.canTransitionTo(SubscriptionStatus.PAST_DUE)).isFalse();
        }

        @Test
        @DisplayName("every state may be cancelled except the one already cancelled")
        void everythingCanEnd() {
            assertThatCode(() -> {
                for (SubscriptionStatus status : SubscriptionStatus.values()) {
                    if (status != SubscriptionStatus.CANCELED) {
                        assertThat(status.canTransitionTo(SubscriptionStatus.CANCELED))
                                .as("%s cannot be cancelled", status)
                                .isTrue();
                    }
                }
            }).doesNotThrowAnyException();
        }
    }

}
