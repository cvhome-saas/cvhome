package com.asrevo.cvhome.billing.service.impl;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.billing.commons.AuditEventType;
import com.asrevo.cvhome.billing.commons.ChangeSource;
import com.asrevo.cvhome.billing.commons.PlanPriceId;
import com.asrevo.cvhome.billing.commons.StripeCustomerId;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.commons.dto.CheckoutSessionView;
import com.asrevo.cvhome.billing.commons.dto.EntitlementSnapshot;
import com.asrevo.cvhome.billing.commons.dto.SubscriptionView;
import com.asrevo.cvhome.billing.commons.errors.BillingProviderUnavailableException;
import com.asrevo.cvhome.billing.commons.errors.IllegalSubscriptionTransitionException;
import com.asrevo.cvhome.billing.commons.errors.PlanPriceNotFoundException;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionChangeRejectedException;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionNotFoundException;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;
import com.asrevo.cvhome.billing.domain.StoreSubscriptionEntity;
import com.asrevo.cvhome.billing.mappers.SubscriptionMappers;
import com.asrevo.cvhome.billing.repository.StoreSubscriptionRepository;
import com.asrevo.cvhome.billing.service.PlanCatalogService;
import com.asrevo.cvhome.billing.service.SubscriptionAuditService;
import com.asrevo.cvhome.billing.service.SubscriptionService;
import com.asrevo.cvhome.billing.service.stripe.StripeCheckoutGateway;
import com.asrevo.cvhome.billing.service.stripe.StripeCustomerGateway;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;

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

    private final SubscriptionAuditService auditService;

    private final SubscriptionMappers mappers;

    @Override
    @Transactional(readOnly = true)
    public SubscriptionView current(ManagerStoreId store, ManagerOrgId scopeOrg)
            throws SubscriptionNotFoundException {
        return mappers.toView(requireInOrg(store, scopeOrg));
    }

    @Override
    @Transactional
    public CheckoutSessionView checkout(ManagerStoreId store, ManagerOrgId scopeOrg, PlanPriceId planPriceId,
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

    @Override
    @Transactional(readOnly = true)
    public EntitlementSnapshot snapshot(ManagerStoreId store) throws SubscriptionNotFoundException {
        return mappers.toSnapshot(require(store));
    }

    @Override
    @Transactional
    public void expireTrial(ManagerStoreId store)
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
    public void suspendUnpaid(ManagerStoreId store)
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

    private StoreSubscriptionEntity require(ManagerStoreId store) throws SubscriptionNotFoundException {
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
    private StoreSubscriptionEntity requireInOrg(ManagerStoreId store, ManagerOrgId scopeOrg)
            throws SubscriptionNotFoundException {
        if (scopeOrg == null) {
            return require(store);
        }
        return subscriptionRepository.findByIdAndOrgId(store, scopeOrg)
                .orElseThrow(() -> SubscriptionNotFoundException.forStore(store));
    }

}
