package com.asrevo.cvhome.billing.service.impl;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.billing.commons.AuditEventType;
import com.asrevo.cvhome.billing.commons.ChangeSource;
import com.asrevo.cvhome.billing.commons.PlanPriceId;
import com.asrevo.cvhome.billing.commons.StripeCustomerId;
import com.asrevo.cvhome.billing.commons.StripeScheduleId;
import com.asrevo.cvhome.billing.commons.StripeSubscriptionId;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.commons.dto.CheckoutSessionView;
import com.asrevo.cvhome.billing.commons.dto.EntitlementSnapshot;
import com.asrevo.cvhome.billing.commons.dto.SubscriptionView;
import com.asrevo.cvhome.billing.commons.errors.BillingProviderUnavailableException;
import com.asrevo.cvhome.billing.commons.errors.IllegalSubscriptionTransitionException;
import com.asrevo.cvhome.billing.commons.errors.ImmediateCancelForbiddenException;
import com.asrevo.cvhome.billing.commons.errors.PlanPriceNotFoundException;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionChangeRejectedException;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionNotFoundException;
import com.asrevo.cvhome.billing.domain.PlanEntity;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;
import com.asrevo.cvhome.billing.domain.StoreSubscriptionEntity;
import com.asrevo.cvhome.billing.mappers.SubscriptionMappers;
import com.asrevo.cvhome.billing.repository.StoreSubscriptionRepository;
import com.asrevo.cvhome.billing.service.PlanCatalogService;
import com.asrevo.cvhome.billing.service.SubscriptionAuditService;
import com.asrevo.cvhome.billing.service.SubscriptionService;
import com.asrevo.cvhome.billing.service.stripe.StripeCheckoutGateway;
import com.asrevo.cvhome.billing.service.stripe.StripeCustomerGateway;
import com.asrevo.cvhome.billing.service.stripe.StripeSubscriptionGateway;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    /**
     * Named as the actor on rows a scheduled job produced, so an audit trail distinguishes "the platform ended this"
     * from "someone ended this".
     */
    private static final String JOB_ACTOR = "billing-job";

    private final StoreSubscriptionRepository subscriptionRepository;

    private final PlanCatalogService planCatalogService;

    private final StripeCustomerGateway customerGateway;

    private final StripeCheckoutGateway checkoutGateway;

    private final StripeSubscriptionGateway subscriptionGateway;

    private final SubscriptionAuditService auditService;

    private final SubscriptionMappers mappers;

    @Override
    @Transactional(readOnly = true)
    public SubscriptionView current(StoreMerchantId store, ManagerOrgId scopeOrg)
            throws SubscriptionNotFoundException {
        return mappers.toView(requireInOrg(store, scopeOrg));
    }

    @Override
    @Transactional
    public CheckoutSessionView checkout(StoreMerchantId store, ManagerOrgId scopeOrg, PlanPriceId planPriceId,
                                        String successUrl, String cancelUrl)
            throws SubscriptionNotFoundException, PlanPriceNotFoundException, SubscriptionChangeRejectedException,
            BillingProviderUnavailableException {
        StoreSubscriptionEntity entity = requireInOrg(store, scopeOrg);
        PlanPriceEntity price = planCatalogService.requirePurchasablePrice(planPriceId);
        if (price.getStripePriceId() == null) {
            // The plan exists locally but was never published. Reported as "not purchasable" rather than as a
            // provider fault, because nothing is wrong with Stripe — the catalog sync has not run.
            throw PlanPriceNotFoundException.of(planPriceId);
        }
        StripeCustomerId customer = customerGateway.findOrCreate(entity.getOrgId(), null);
        if (!customer.equals(entity.getStripeCustomerId())) {
            subscriptionRepository.save(entity.bindCustomer(customer));
        }
        String url = checkoutGateway.createSubscriptionSession(store, entity.getOrgId(), customer, price,
                successUrl, cancelUrl);
        return new CheckoutSessionView(url);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The ordering here carries the whole "don't half-apply a change" rule. Stripe is called first and the local row
     * is written only after it answers, so a refusal leaves the customer exactly where they were, and an <em>unknown</em>
     * outcome leaves the row untouched for the webhook to settle. Writing locally first and reconciling later would
     * show a customer a plan they had not bought.
     * </p>
     */
    @Override
    @Transactional
    public SubscriptionView changePlan(StoreMerchantId store, ManagerOrgId scopeOrg, PlanPriceId targetPriceId)
            throws SubscriptionNotFoundException, PlanPriceNotFoundException, SubscriptionChangeRejectedException,
            BillingProviderUnavailableException, IllegalSubscriptionTransitionException {
        StoreSubscriptionEntity entity = requireInOrg(store, scopeOrg);
        PlanPriceEntity target = planCatalogService.requirePurchasablePrice(targetPriceId);
        requirePublished(target, targetPriceId);
        StripeSubscriptionId subscription = requireProviderSubscription(entity);

        PlanPriceEntity current = planCatalogService.requirePrice(entity.getPlanPriceId());
        if (target.getId().equals(current.getId())) {
            return mappers.toView(entity);
        }
        SubscriptionStatus before = entity.getStatus();
        return upgrades(current, target)
                ? applyUpgrade(entity, subscription, target, before)
                : applyDowngrade(entity, subscription, target, before);
    }

    @Override
    @Transactional
    public SubscriptionView cancel(StoreMerchantId store, ManagerOrgId scopeOrg, boolean immediate,
                                   boolean superAdmin)
            throws SubscriptionNotFoundException, BillingProviderUnavailableException,
            IllegalSubscriptionTransitionException, ImmediateCancelForbiddenException {
        StoreSubscriptionEntity entity = requireInOrg(store, scopeOrg);
        if (immediate && !superAdmin) {
            throw ImmediateCancelForbiddenException.forStore(store);
        }
        StripeSubscriptionId subscription = requireProviderSubscription(entity);
        SubscriptionStatus before = entity.getStatus();
        if (immediate) {
            subscriptionGateway.cancelNow(store, subscription);
            StoreSubscriptionEntity saved = subscriptionRepository.save(reload(entity).cancelNow(Instant.now()));
            auditService.record(before, saved, AuditEventType.CANCELED, ChangeSource.API, null);
            return mappers.toView(saved);
        }
        // A scheduled subscription will not accept cancellation behaviour directly — Stripe refuses it and says to
        // change the schedule instead. Releasing it first is also the right product answer: asking not to renew makes
        // a deferred downgrade moot, because it would land at the very moment the subscription ends.
        releaseAnySchedule(entity);
        subscriptionGateway.setRenewal(store, subscription, false);
        StoreSubscriptionEntity saved = subscriptionRepository.save(reload(entity).scheduleCancel());
        auditService.record(before, saved, AuditEventType.CANCEL_SCHEDULED, ChangeSource.API, null);
        log.info("Store {} will not renew after {}", store, saved.getCurrentPeriodEnd());
        return mappers.toView(saved);
    }

    @Override
    @Transactional
    public SubscriptionView resume(StoreMerchantId store, ManagerOrgId scopeOrg)
            throws SubscriptionNotFoundException, BillingProviderUnavailableException,
            IllegalSubscriptionTransitionException {
        StoreSubscriptionEntity entity = requireInOrg(store, scopeOrg);
        StripeSubscriptionId subscription = requireProviderSubscription(entity);
        // Checked before anything is sent to the provider, and the order is the point: an earlier version released
        // the schedule first and only then discovered there was nothing to resume, so the request failed with Stripe
        // already changed. Refusing up front leaves both sides untouched.
        if (!entity.isCancelAtPeriodEnd() && entity.getPendingPlanPriceId() == null) {
            throw IllegalSubscriptionTransitionException.of(store, entity.getStatus(), "RESUMED");
        }
        SubscriptionStatus before = entity.getStatus();

        // A pending downgrade is called off too. Resuming means "carry on as I am", and leaving a scheduled drop in
        // place would surprise the customer at the period boundary with a change they thought they had undone.
        releaseAnySchedule(entity);
        subscriptionGateway.setRenewal(store, subscription, true);
        StoreSubscriptionEntity saved = subscriptionRepository.save(reload(entity).revokeScheduledCancel());
        auditService.record(before, saved, AuditEventType.CANCEL_REVOKED, ChangeSource.API, null);
        return mappers.toView(saved);
    }

    @Override
    @Transactional
    public void applyPendingChange(StoreMerchantId store)
            throws SubscriptionNotFoundException, PlanPriceNotFoundException {
        StoreSubscriptionEntity entity = require(store);
        if (entity.getPendingPlanPriceId() == null) {
            // The webhook got here first, which is the normal case. This job only exists for the one that never came.
            return;
        }
        PlanPriceEntity target = planCatalogService.requirePrice(entity.getPendingPlanPriceId());
        SubscriptionStatus before = entity.getStatus();
        StoreSubscriptionEntity saved = subscriptionRepository.save(
                entity.applyPendingChange(target.getPlanId(), target.getId()));
        auditService.record(before, saved, AuditEventType.PLAN_DOWNGRADE_APPLIED, ChangeSource.JOB, JOB_ACTOR);
        log.info("Applied the deferred plan change on store {}", store);
    }

    @Override
    @Transactional(readOnly = true)
    public EntitlementSnapshot snapshot(StoreMerchantId store) throws SubscriptionNotFoundException {
        return mappers.toSnapshot(require(store));
    }

    @Override
    @Transactional
    public void expireTrial(StoreMerchantId store)
            throws SubscriptionNotFoundException, IllegalSubscriptionTransitionException {
        StoreSubscriptionEntity entity = require(store);
        if (entity.getStatus() != SubscriptionStatus.TRIALING) {
            // The store paid, or was cancelled, between the job noticing and the command being handled. Not an error:
            // the job runs on a schedule and the world moves underneath it.
            log.info("Store {} is {} rather than trialling — nothing to expire", store, entity.getStatus());
            return;
        }
        suspend(entity, AuditEventType.SUSPENDED);
    }

    @Override
    @Transactional
    public void suspendUnpaid(StoreMerchantId store)
            throws SubscriptionNotFoundException, IllegalSubscriptionTransitionException {
        StoreSubscriptionEntity entity = require(store);
        if (entity.getStatus() != SubscriptionStatus.PAST_DUE) {
            log.info("Store {} is {} rather than past due — nothing to suspend", store, entity.getStatus());
            return;
        }
        suspend(entity, AuditEventType.SUSPENDED);
    }

    private void suspend(StoreSubscriptionEntity entity, AuditEventType eventType)
            throws IllegalSubscriptionTransitionException {
        SubscriptionStatus before = entity.getStatus();
        StoreSubscriptionEntity saved = subscriptionRepository.save(entity.suspend(Instant.now()));
        auditService.record(before, saved, eventType, ChangeSource.JOB, JOB_ACTOR);
        log.info("Suspended store {} (was {})", saved.getId(), before);
    }

    /**
     * Whether moving from {@code current} to {@code target} is a move up.
     *
     * <p>
     * Tier decides it, and price breaks the tie so a same-plan interval switch still has an answer: monthly to yearly
     * costs more up front and so is charged now, yearly to monthly is deferred like any other reduction.
     * </p>
     */
    private boolean upgrades(PlanPriceEntity current, PlanPriceEntity target) {
        PlanEntity currentPlan = planCatalogService.findPlan(current.getPlanId()).orElse(null);
        PlanEntity targetPlan = planCatalogService.findPlan(target.getPlanId()).orElse(null);
        if (currentPlan != null && targetPlan != null && !targetPlan.getTier().equals(currentPlan.getTier())) {
            return targetPlan.getTier() > currentPlan.getTier();
        }
        return target.getUnitAmount() > current.getUnitAmount();
    }

    private SubscriptionView applyUpgrade(StoreSubscriptionEntity stale, StripeSubscriptionId subscription,
                                          PlanPriceEntity target, SubscriptionStatus before)
            throws SubscriptionChangeRejectedException, BillingProviderUnavailableException,
            SubscriptionNotFoundException {
        subscriptionGateway.upgradeNow(stale.getId(), subscription, target);
        StoreSubscriptionEntity entity = reload(stale);
        StoreSubscriptionEntity saved = subscriptionRepository.save(entity.upgradeTo(target.getPlanId(),
                target.getId(), entity.getCurrentPeriodStart(), entity.getCurrentPeriodEnd()));
        auditService.record(before, saved, AuditEventType.PLAN_UPGRADED, ChangeSource.API, null);
        log.info("Store {} upgraded to {}", entity.getId(), target.getId());
        return mappers.toView(saved);
    }

    /**
     * Records a downgrade as pending rather than applying it.
     *
     * <p>
     * Entitlements do not narrow yet — the customer keeps what they paid for until the period ends. The plan changes
     * when the provider's schedule fires, or when the safety-net job notices the date has passed.
     * </p>
     */
    private SubscriptionView applyDowngrade(StoreSubscriptionEntity stale, StripeSubscriptionId subscription,
                                            PlanPriceEntity target, SubscriptionStatus before)
            throws BillingProviderUnavailableException, SubscriptionNotFoundException {
        Instant effectiveAt = stale.getCurrentPeriodEnd();
        StripeScheduleId schedule = subscriptionGateway.scheduleDowngrade(stale.getId(), subscription, target,
                effectiveAt);
        StoreSubscriptionEntity entity = reload(stale);
        StoreSubscriptionEntity saved = subscriptionRepository.save(
                entity.scheduleDowngradeTo(target.getId(), effectiveAt).bindSchedule(schedule));
        auditService.record(before, saved, AuditEventType.PLAN_DOWNGRADE_SCHEDULED, ChangeSource.API, null);
        log.info("Store {} will move to {} at {}", entity.getId(), target.getId(), effectiveAt);
        return mappers.toView(saved);
    }

    /**
     * The provider subscription a change acts on.
     *
     * <p>
     * Absent means the store has never actually bought anything — it is unpaid, or its trial has not converted — so
     * there is nothing at the provider to move, switch off or end. Reported as an illegal transition rather than as a
     * missing subscription, because the row is right there; it is the state that makes the request meaningless.
     * </p>
     */
    private StripeSubscriptionId requireProviderSubscription(StoreSubscriptionEntity entity)
            throws IllegalSubscriptionTransitionException {
        if (entity.getStripeSubscriptionId() == null) {
            throw IllegalSubscriptionTransitionException.of(entity.getId(), entity.getStatus(), "PROVIDER_CHANGE");
        }
        return entity.getStripeSubscriptionId();
    }

    private void requirePublished(PlanPriceEntity price, PlanPriceId requested) throws PlanPriceNotFoundException {
        if (price.getStripePriceId() == null) {
            throw PlanPriceNotFoundException.of(requested);
        }
    }

    /**
     * Drops the provider schedule backing a deferred change, if there is one.
     *
     * <p>
     * Needed before any cancellation change, because Stripe will not let a scheduled subscription's cancellation
     * behaviour be set directly — it insists the schedule is changed instead. Releasing hands the subscription back
     * to ordinary renewal, which is the state both cancelling and resuming want to start from.
     * </p>
     */
    private void releaseAnySchedule(StoreSubscriptionEntity entity) throws BillingProviderUnavailableException {
        if (entity.getStripeScheduleId() != null) {
            subscriptionGateway.releaseSchedule(entity.getId(), entity.getStripeScheduleId());
        }
    }

    /**
     * Re-reads the aggregate after a provider call, so the local write is applied to current state.
     *
     * <p>
     * Not defensive padding — a required step. Every mutation at Stripe immediately emits
     * {@code customer.subscription.updated}, whose outbox handler writes to this very row, and it routinely commits
     * before the request that caused it gets back. Saving the instance loaded before the call then fails on the
     * version check, and the failure lands <em>after</em> Stripe has already changed: the provider would hold a
     * schedule, or a new price, that our row knows nothing about.
     * </p>
     */
    private StoreSubscriptionEntity reload(StoreSubscriptionEntity stale) throws SubscriptionNotFoundException {
        return require(stale.getId());
    }

    private StoreSubscriptionEntity require(StoreMerchantId store) throws SubscriptionNotFoundException {
        return subscriptionRepository.findById(store)
                .orElseThrow(() -> SubscriptionNotFoundException.forStore(store));
    }

    /**
     * Loads a subscription within the caller's tenant.
     *
     * <p>
     * A {@code null} org means the caller spans orgs — a platform admin or another cvhome service — and only the
     * controller may decide that. Everyone else gets a query that cannot reach another org's row, which is what makes
     * the boundary hold even though the shared permission checker cannot yet tell which org a store belongs to.
     * </p>
     */
    private StoreSubscriptionEntity requireInOrg(StoreMerchantId store, ManagerOrgId scopeOrg)
            throws SubscriptionNotFoundException {
        if (scopeOrg == null) {
            return require(store);
        }
        return subscriptionRepository.findByIdAndOrgId(store, scopeOrg)
                .orElseThrow(() -> SubscriptionNotFoundException.forStore(store));
    }

}
