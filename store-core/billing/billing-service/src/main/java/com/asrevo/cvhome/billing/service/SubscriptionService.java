package com.asrevo.cvhome.billing.service;

import com.asrevo.cvhome.billing.commons.dto.EntitlementSnapshot;
import com.asrevo.cvhome.billing.commons.dto.SubscriptionView;
import com.asrevo.cvhome.billing.commons.errors.IllegalSubscriptionTransitionException;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionNotFoundException;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;

/**
 * Reads and lifecycle transitions of one store's subscription.
 */
public interface SubscriptionService {

    /**
     * What the store is on, and what happens next.
     *
     * <p>
     * {@code scopeOrg} is the tenant boundary and is not optional in spirit: pass the caller's org so the lookup can
     * only ever find a store that org owns. Only a principal that legitimately spans orgs — a platform admin, or
     * another cvhome service — may pass {@code null}, and the controller is what decides that.
     * </p>
     *
     * @param scopeOrg the org the caller belongs to, or {@code null} for a caller that spans orgs
     * @throws SubscriptionNotFoundException billing has never seen this store, or it belongs to another org. The two
     *                                       are deliberately indistinguishable — answering "it exists but is not
     *                                       yours" would confirm another org's store to someone who cannot see it
     */
    SubscriptionView current(ManagerStoreId store, ManagerOrgId scopeOrg) throws SubscriptionNotFoundException;

    /**
     * What the store is allowed to do, for the enforcement layers.
     *
     * @throws SubscriptionNotFoundException billing has never seen this store
     */
    EntitlementSnapshot snapshot(ManagerStoreId store) throws SubscriptionNotFoundException;

    /**
     * Ends a trial that ran out without a payment.
     *
     * @throws SubscriptionNotFoundException          the store disappeared between the job and the command
     * @throws IllegalSubscriptionTransitionException the subscription moved on before the command was handled
     */
    void expireTrial(ManagerStoreId store)
            throws SubscriptionNotFoundException, IllegalSubscriptionTransitionException;

    /**
     * Suspends a store whose grace window after a failed renewal has closed.
     *
     * @throws SubscriptionNotFoundException          the store disappeared between the job and the command
     * @throws IllegalSubscriptionTransitionException the subscription moved on before the command was handled
     */
    void suspendUnpaid(ManagerStoreId store)
            throws SubscriptionNotFoundException, IllegalSubscriptionTransitionException;

}
