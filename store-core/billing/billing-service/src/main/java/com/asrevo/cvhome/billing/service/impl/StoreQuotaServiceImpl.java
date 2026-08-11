package com.asrevo.cvhome.billing.service.impl;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.billing.commons.AuditEventType;
import com.asrevo.cvhome.billing.commons.ChangeSource;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.commons.dto.StoreQuotaDecision;
import com.asrevo.cvhome.billing.commons.dto.SubscriptionView;
import com.asrevo.cvhome.billing.commons.errors.PlanNotFoundException;
import com.asrevo.cvhome.billing.config.BillingProperties;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;
import com.asrevo.cvhome.billing.domain.StoreSubscriptionEntity;
import com.asrevo.cvhome.billing.mappers.SubscriptionMappers;
import com.asrevo.cvhome.billing.repository.OrgTrialGrantRepository;
import com.asrevo.cvhome.billing.repository.StoreSubscriptionRepository;
import com.asrevo.cvhome.billing.service.PlanCatalogService;
import com.asrevo.cvhome.billing.service.StoreQuotaService;
import com.asrevo.cvhome.billing.service.SubscriptionAuditService;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Decides whether an org may create another store, and gives a store its subscription once it exists.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoreQuotaServiceImpl implements StoreQuotaService {

    /**
     * Refusal reason when the org is holding more stores it has never paid for than it is allowed to.
     */
    private static final String REASON_TOO_MANY_PENDING = "TOO_MANY_PENDING_STORES";

    private final StoreSubscriptionRepository subscriptionRepository;

    private final OrgTrialGrantRepository trialGrantRepository;

    private final PlanCatalogService planCatalogService;

    private final SubscriptionAuditService auditService;

    private final SubscriptionMappers mappers;

    private final BillingProperties properties;

    /**
     * {@inheritDoc}
     *
     * <p>
     * Not a cap on how many stores an org may own — each store carries its own subscription and pays for itself. The
     * only thing refused here is stockpiling stores nobody ever pays for.
     * </p>
     */
    @Override
    @Transactional(readOnly = true)
    public StoreQuotaDecision checkStoreCreate(ManagerOrgId org) {
        int pending = subscriptionRepository.countByOrgIdAndStatus(org, SubscriptionStatus.PENDING);
        boolean trialAvailable = !trialGrantRepository.existsById(org);
        int max = properties.quota().maxPendingStores();
        if (pending >= max) {
            log.info("Refusing another store for org {}: {} unpaid stores, limit {}", org, pending, max);
            return StoreQuotaDecision.refuse(REASON_TOO_MANY_PENDING, trialAvailable, pending);
        }
        return StoreQuotaDecision.allow(trialAvailable, pending);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The org-trial-once rule is the {@code claim} call below, and nothing else. Two stores created for the same org
     * at the same instant both attempt that insert; the primary key lets exactly one through, and the loser's store
     * starts unpaid. Reading "has this org had a trial?" and then writing would lose that race, and losing it means
     * giving away free months.
     * </p>
     */
    @Override
    @Transactional
    public SubscriptionView provision(ManagerOrgId org, ManagerStoreId store) throws PlanNotFoundException {
        var existing = subscriptionRepository.findById(store);
        if (existing.isPresent()) {
            // Provisioning arrives from an outbox handler, so a repeat is routine rather than exceptional.
            log.debug("Store {} already has a subscription — nothing to provision", store);
            return mappers.toView(existing.get());
        }
        Instant trialEnd = Instant.now().plus(properties.trialPeriod());
        StoreSubscriptionEntity entity = claimTrial(org, store, trialEnd)
                ? trialSubscription(org, store, trialEnd)
                : StoreSubscriptionEntity.pending(store, org);
        StoreSubscriptionEntity saved = subscriptionRepository.save(entity);
        boolean trialing = saved.getStatus() == SubscriptionStatus.TRIALING;
        auditService.record(null, saved,
                trialing ? AuditEventType.TRIAL_STARTED : AuditEventType.CREATED, ChangeSource.SYSTEM, null);
        log.info("Provisioned store {} of org {} as {}", store, org, saved.getStatus());
        return mappers.toView(saved);
    }

    private StoreSubscriptionEntity trialSubscription(ManagerOrgId org, ManagerStoreId store, Instant trialEnd)
            throws PlanNotFoundException {
        PlanPriceEntity trialPrice = planCatalogService.cheapestActivePrice()
                .orElseThrow(() -> PlanNotFoundException.byCode("cheapest-active"));
        return StoreSubscriptionEntity.trialing(store, org, trialPrice.getPlanId(), trialPrice.getId(), trialEnd);
    }

    /**
     * @return whether this call is the one that spent the org's trial
     */
    private boolean claimTrial(ManagerOrgId org, ManagerStoreId store, Instant trialEnd) {
        boolean claimed = trialGrantRepository.claim(org.getId().toString(), store.getId().toString(), Instant.now(),
                trialEnd) == 1;
        if (!claimed) {
            log.info("Org {} has already used its trial — store {} starts unpaid", org, store);
        }
        return claimed;
    }

}
