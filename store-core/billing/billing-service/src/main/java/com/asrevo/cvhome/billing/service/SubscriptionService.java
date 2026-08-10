package com.asrevo.cvhome.billing.service;

import com.asrevo.cvhome.billing.commons.PlanPriceId;
import com.asrevo.cvhome.billing.commons.dto.CheckoutSessionView;
import com.asrevo.cvhome.billing.commons.dto.EntitlementSnapshot;
import com.asrevo.cvhome.billing.commons.dto.SubscriptionView;
import com.asrevo.cvhome.billing.commons.errors.BillingProviderUnavailableException;
import com.asrevo.cvhome.billing.commons.errors.IllegalSubscriptionTransitionException;
import com.asrevo.cvhome.billing.commons.errors.PlanPriceNotFoundException;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionChangeRejectedException;
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
     * Opens a Stripe checkout so the store can start paying, and says where to send the customer.
     *
     * <p>
     * Changes nothing locally. The subscription becomes active when Stripe reports the money moved, not when the
     * customer is redirected — otherwise a store that abandoned the payment page would come back activated.
     * </p>
     *
     * @param scopeOrg  the caller's org; the store must belong to it
     * @throws SubscriptionNotFoundException        billing has never seen this store, or it is not this caller's
     * @throws PlanPriceNotFoundException           the price is absent or no longer on sale
     * @throws SubscriptionChangeRejectedException  the provider refused outright
     * @throws BillingProviderUnavailableException  the provider could not be reached, so nothing was decided
     */
    CheckoutSessionView checkout(ManagerStoreId store, ManagerOrgId scopeOrg, PlanPriceId planPriceId,
                                 String successUrl, String cancelUrl)
            throws SubscriptionNotFoundException, PlanPriceNotFoundException, SubscriptionChangeRejectedException,
            BillingProviderUnavailableException;

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
