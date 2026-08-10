package com.asrevo.cvhome.billing.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.asrevo.cvhome.billing.commons.StripeCustomerId;
import com.asrevo.cvhome.billing.commons.StripeSubscriptionId;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.domain.StoreSubscriptionEntity;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;

public interface StoreSubscriptionRepository extends CrudRepository<StoreSubscriptionEntity, ManagerStoreId> {

    List<StoreSubscriptionEntity> findAllByOrgId(ManagerOrgId orgId);

    /**
     * Loads a store's subscription only if it belongs to {@code orgId}.
     *
     * <p>
     * The tenant boundary, enforced in the query rather than trusted to the permission layer.
     * {@code StoreRoleAccessChecker.isOrgAdmin} currently returns true for any store once the caller holds the
     * org-admin role — it has no way to map a store to its org, and says so in a {@code TODO}. Billing does have that
     * mapping, in {@code org_id} on this very row, so it uses it. Without this, one org's admin could read another
     * org's plan, spend and invoices.
     * </p>
     */
    Optional<StoreSubscriptionEntity> findByIdAndOrgId(ManagerStoreId id, ManagerOrgId orgId);

    int countByOrgIdAndStatus(ManagerOrgId orgId, SubscriptionStatus status);

    Optional<StoreSubscriptionEntity> findByStripeSubscriptionId(StripeSubscriptionId stripeSubscriptionId);

    /**
     * Whether this provider customer is one of ours — used to tell an invoice we cannot attribute <em>yet</em> from
     * one that was never ours to begin with.
     */
    boolean existsByStripeCustomerId(StripeCustomerId stripeCustomerId);

    Optional<StoreSubscriptionEntity> findFirstByOrgIdAndStripeCustomerIdNotNull(ManagerOrgId orgId);

    List<StoreSubscriptionEntity> findAllByStatusAndTrialEndBefore(SubscriptionStatus status, Instant before);

    List<StoreSubscriptionEntity> findAllByStatusAndGraceUntilBefore(SubscriptionStatus status, Instant before);

    List<StoreSubscriptionEntity> findAllByPendingEffectiveAtBefore(Instant before);

    /**
     * The stores no enforcement layer should let through. Kept as a projection of ids rather than whole rows: the
     * gateway polls this once a minute for every blocked store on the platform.
     */
    List<StoreSubscriptionEntity> findAllByStatusIn(List<SubscriptionStatus> statuses);

    /**
     * @return the customer this org already has at the provider, if any of its stores has one
     */
    default Optional<StripeCustomerId> findCustomerOf(ManagerOrgId orgId) {
        return findFirstByOrgIdAndStripeCustomerIdNotNull(orgId)
                .map(StoreSubscriptionEntity::getStripeCustomerId);
    }

}
