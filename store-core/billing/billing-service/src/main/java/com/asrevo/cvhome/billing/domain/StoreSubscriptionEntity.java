package com.asrevo.cvhome.billing.domain;

import java.time.Instant;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

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
import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import lombok.Getter;

/**
 * One store's subscription, keyed by the store it pays for.
 *
 * <p>
 * There are no setters. Every change is a named method that checks the transition is legal, mutates, registers the
 * event and returns {@code this} — so a caller cannot put the row in a state the state machine forbids, and every
 * state change has exactly one place it can have come from.
 * </p>
 *
 * <p>
 * Moving to the state it is already in is a no-op rather than a failure, everywhere. Stripe redelivers webhooks, and
 * an event arriving twice has to leave the row and the event stream unchanged.
 * </p>
 */
@Getter
@Table(schema = "billing", name = "store_subscription")
public class StoreSubscriptionEntity extends BaseEntity<StoreSubscriptionEntity, StoreMerchantId> {

    @Column("org_id")
    private ManagerOrgId orgId;

    @Column("status")
    private SubscriptionStatus status;

    @Column("plan_id")
    private PlanId planId;

    @Column("plan_price_id")
    private PlanPriceId planPriceId;

    @Column("stripe_customer_id")
    private StripeCustomerId stripeCustomerId;

    @Column("stripe_subscription_id")
    private StripeSubscriptionId stripeSubscriptionId;

    @Column("stripe_schedule_id")
    private StripeScheduleId stripeScheduleId;

    @Column("current_period_start")
    private Instant currentPeriodStart;

    @Column("current_period_end")
    private Instant currentPeriodEnd;

    @Column("trial_end")
    private Instant trialEnd;

    @Column("cancel_at_period_end")
    private boolean cancelAtPeriodEnd;

    @Column("canceled_at")
    private Instant canceledAt;

    @Column("suspended_at")
    private Instant suspendedAt;

    @Column("pending_plan_price_id")
    private PlanPriceId pendingPlanPriceId;

    @Column("pending_effective_at")
    private Instant pendingEffectiveAt;

    @Column("grace_until")
    private Instant graceUntil;

    @Column("created_date")
    private Instant createdDate;

    @Column("updated_date")
    private Instant updatedDate;

    /**
     * A store that has to be paid for before it can be used — what an org gets once it has spent its one trial.
     */
    public static StoreSubscriptionEntity pending(StoreMerchantId store, ManagerOrgId org) {
        StoreSubscriptionEntity entity = newRow(store, org);
        entity.status = SubscriptionStatus.PENDING;
        return entity;
    }

    /**
     * The org's one trial, spent on this store.
     */
    public static StoreSubscriptionEntity trialing(StoreMerchantId store, ManagerOrgId org, PlanId plan,
                                                   PlanPriceId price, Instant trialEnd) {
        StoreSubscriptionEntity entity = newRow(store, org);
        entity.status = SubscriptionStatus.TRIALING;
        entity.planId = plan;
        entity.planPriceId = price;
        entity.trialEnd = trialEnd;
        entity.currentPeriodStart = entity.createdDate;
        entity.currentPeriodEnd = trialEnd;
        entity.registerEvent(SubscriptionTrialStartedEvent.from(store, org));
        return entity;
    }

    private static StoreSubscriptionEntity newRow(StoreMerchantId store, ManagerOrgId org) {
        StoreSubscriptionEntity entity = new StoreSubscriptionEntity();
        Instant now = Instant.now();
        entity.setId(store);
        entity.orgId = org;
        entity.cancelAtPeriodEnd = false;
        entity.createdDate = now;
        entity.updatedDate = now;
        return entity;
    }

    /**
     * Records the provider identifiers a checkout produced. Not a state change: the money has not moved yet, and the
     * invoice event is what says it has.
     */
    public StoreSubscriptionEntity bindProvider(StripeCustomerId customer, StripeSubscriptionId subscription) {
        this.stripeCustomerId = customer;
        this.stripeSubscriptionId = subscription;
        this.updatedDate = Instant.now();
        return this;
    }

    /**
     * Records the org's provider customer without touching the subscription — used when the customer is created
     * ahead of a checkout.
     */
    public StoreSubscriptionEntity bindCustomer(StripeCustomerId customer) {
        this.stripeCustomerId = customer;
        this.updatedDate = Instant.now();
        return this;
    }

    /**
     * The first paid period opened, or a suspended store came back after paying.
     */
    public StoreSubscriptionEntity activate(PlanId plan, PlanPriceId price, Instant periodStart, Instant periodEnd)
            throws IllegalSubscriptionTransitionException {
        if (this.status == SubscriptionStatus.ACTIVE) {
            return renew(periodStart, periodEnd);
        }
        require(SubscriptionStatus.ACTIVE);
        this.status = SubscriptionStatus.ACTIVE;
        this.planId = plan;
        this.planPriceId = price;
        this.currentPeriodStart = periodStart;
        this.currentPeriodEnd = periodEnd;
        this.graceUntil = null;
        this.suspendedAt = null;
        this.trialEnd = null;
        this.updatedDate = Instant.now();
        this.registerEvent(SubscriptionActivatedEvent.from(this.id, this.orgId));
        return this;
    }

    /**
     * A paid period rolled into the next one.
     */
    public StoreSubscriptionEntity renew(Instant periodStart, Instant periodEnd) {
        this.status = SubscriptionStatus.ACTIVE;
        this.currentPeriodStart = periodStart;
        this.currentPeriodEnd = periodEnd;
        this.graceUntil = null;
        this.updatedDate = Instant.now();
        this.registerEvent(SubscriptionRenewedEvent.from(this.id, this.orgId));
        return this;
    }

    /**
     * A renewal failed. The store stays usable until {@code graceUntil} — a merchant who cannot trade cannot earn the
     * money to settle the invoice.
     */
    public StoreSubscriptionEntity markPastDue(Instant graceUntil)
            throws IllegalSubscriptionTransitionException {
        if (this.status == SubscriptionStatus.PAST_DUE) {
            return this;
        }
        require(SubscriptionStatus.PAST_DUE);
        this.status = SubscriptionStatus.PAST_DUE;
        this.graceUntil = graceUntil;
        this.updatedDate = Instant.now();
        this.registerEvent(SubscriptionPastDueEvent.from(this.id, this.orgId));
        return this;
    }

    /**
     * Access ends: a trial that ran out, or a grace window that closed.
     */
    public StoreSubscriptionEntity suspend(Instant at) throws IllegalSubscriptionTransitionException {
        if (this.status == SubscriptionStatus.SUSPENDED) {
            return this;
        }
        require(SubscriptionStatus.SUSPENDED);
        this.status = SubscriptionStatus.SUSPENDED;
        this.suspendedAt = at;
        this.updatedDate = Instant.now();
        this.registerEvent(SubscriptionSuspendedEvent.from(this.id, this.orgId));
        return this;
    }

    /**
     * A move to a more expensive plan, which takes effect at once because it has already been paid for.
     */
    public StoreSubscriptionEntity upgradeTo(PlanId plan, PlanPriceId price, Instant periodStart, Instant periodEnd) {
        this.planId = plan;
        this.planPriceId = price;
        this.currentPeriodStart = periodStart;
        this.currentPeriodEnd = periodEnd;
        this.clearPendingChange();
        this.updatedDate = Instant.now();
        this.registerEvent(SubscriptionPlanChangedEvent.from(this.id, this.orgId));
        return this;
    }

    /**
     * A move to a cheaper plan, deferred to the end of the period the customer already paid for. Registers no event:
     * nothing has changed for anyone reading entitlements yet.
     */
    public StoreSubscriptionEntity scheduleDowngradeTo(PlanPriceId price, Instant effectiveAt) {
        this.pendingPlanPriceId = price;
        this.pendingEffectiveAt = effectiveAt;
        this.updatedDate = Instant.now();
        return this;
    }

    /**
     * A deferred change came due.
     *
     * <p>
     * Idempotent, and it has to be: the provider's webhook and the safety-net job both land here, and whichever
     * arrives second finds nothing pending and does nothing.
     * </p>
     */
    public StoreSubscriptionEntity applyPendingChange(PlanId plan, PlanPriceId price) {
        if (this.pendingPlanPriceId == null) {
            return this;
        }
        this.planId = plan;
        this.planPriceId = price;
        this.clearPendingChange();
        this.updatedDate = Instant.now();
        this.registerEvent(SubscriptionPlanChangedEvent.from(this.id, this.orgId));
        return this;
    }

    /**
     * Renewal switched off. The status does not change: the customer keeps everything until the period they paid for
     * runs out, which is what separates this from cancelling.
     *
     * <p>
     * Any pending plan change is dropped, because it can no longer happen — a deferred downgrade takes effect at the
     * period boundary, which is exactly when this subscription now ends. Keeping it would leave a change scheduled
     * for a subscription that will not be there.
     * </p>
     */
    public StoreSubscriptionEntity scheduleCancel() {
        this.cancelAtPeriodEnd = true;
        this.clearPendingChange();
        this.updatedDate = Instant.now();
        return this;
    }

    /**
     * Mirrors the provider's renewal flag, in whichever direction it moved.
     *
     * <p>
     * For the webhook path only, and deliberately without the guard {@link #revokeScheduledCancel} carries. The
     * provider is the authority on whether this subscription will renew, so reconciliation has to be able to clear
     * the flag as well as set it. An earlier version only ever set it, which left a resumed subscription reading as
     * "will not renew" forever once a late webhook from the cancel arrived after the resume.
     * </p>
     */
    public StoreSubscriptionEntity reconcileRenewal(boolean providerCancelAtPeriodEnd) {
        if (this.cancelAtPeriodEnd == providerCancelAtPeriodEnd) {
            return this;
        }
        this.cancelAtPeriodEnd = providerCancelAtPeriodEnd;
        if (providerCancelAtPeriodEnd) {
            this.clearPendingChange();
        }
        this.updatedDate = Instant.now();
        return this;
    }

    /**
     * Renewal switched back on.
     */
    public StoreSubscriptionEntity revokeScheduledCancel() throws IllegalSubscriptionTransitionException {
        if (!this.cancelAtPeriodEnd) {
            throw IllegalSubscriptionTransitionException.of(this.id, this.status, "RESUMED");
        }
        this.cancelAtPeriodEnd = false;
        this.updatedDate = Instant.now();
        return this;
    }

    /**
     * The subscription ended.
     */
    public StoreSubscriptionEntity cancelNow(Instant at) throws IllegalSubscriptionTransitionException {
        if (this.status == SubscriptionStatus.CANCELED) {
            return this;
        }
        require(SubscriptionStatus.CANCELED);
        this.status = SubscriptionStatus.CANCELED;
        this.canceledAt = at;
        this.cancelAtPeriodEnd = false;
        this.clearPendingChange();
        this.updatedDate = Instant.now();
        this.registerEvent(SubscriptionCanceledEvent.from(this.id, this.orgId));
        return this;
    }

    /**
     * Returns a cancelled subscription to unpaid, ready to be bought again. The row and its audit trail are kept —
     * the history of a store that once paid is worth more than a clean slate.
     */
    public StoreSubscriptionEntity reopen() throws IllegalSubscriptionTransitionException {
        require(SubscriptionStatus.PENDING);
        this.status = SubscriptionStatus.PENDING;
        this.stripeSubscriptionId = null;
        this.stripeScheduleId = null;
        this.canceledAt = null;
        this.suspendedAt = null;
        this.graceUntil = null;
        this.currentPeriodStart = null;
        this.currentPeriodEnd = null;
        this.clearPendingChange();
        this.updatedDate = Instant.now();
        return this;
    }

    /**
     * Records the provider-side schedule backing a deferred downgrade.
     */
    public StoreSubscriptionEntity bindSchedule(StripeScheduleId schedule) {
        this.stripeScheduleId = schedule;
        this.updatedDate = Instant.now();
        return this;
    }

    /**
     * Whether the store may be worked in right now.
     */
    public boolean operable() {
        return status.operable();
    }

    private void clearPendingChange() {
        this.pendingPlanPriceId = null;
        this.pendingEffectiveAt = null;
        this.stripeScheduleId = null;
    }

    private void require(SubscriptionStatus target) throws IllegalSubscriptionTransitionException {
        if (!this.status.canTransitionTo(target)) {
            throw IllegalSubscriptionTransitionException.of(this.id, this.status, target);
        }
    }

    @Override
    protected StoreMerchantId generateId() {
        return id;
    }

}
