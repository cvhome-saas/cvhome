package com.asrevo.cvhome.billing.commons.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

import com.asrevo.cvhome.billing.commons.EntitlementKey;
import com.asrevo.cvhome.billing.commons.EntitlementValue;
import com.asrevo.cvhome.billing.commons.Money;
import com.asrevo.cvhome.billing.commons.PlanPriceId;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;

/**
 * A store's subscription as its owner sees it — the response behind "what am I on, and what happens next".
 *
 * @param store              the store
 * @param status             its state
 * @param planCode           the plan handle, or null before anything was ever chosen
 * @param planDisplayName    what to show for the plan
 * @param planPriceId        the price in force
 * @param amount             what it charges
 * @param currentPeriodEnd   the next renewal date, or the date access ends when {@code cancelAtPeriodEnd}
 * @param trialEnd           when the trial runs out, if this is a trial
 * @param cancelAtPeriodEnd  whether renewal has been switched off
 * @param graceUntil         how long a failed renewal has left before suspension
 * @param pendingPlanChange  a downgrade waiting for the period to end, if any
 * @param providerLinked     whether a provider subscription stands behind this row, and so whether changing plan,
 *                           cancelling or resuming is possible at all — a trial granted by us has none, and must be
 *                           bought through checkout rather than switched. Status does not answer this: a store is
 *                           {@code TRIALING} both before it has ever paid and, in principle, on a provider-run trial
 * @param entitlements       what the current plan grants
 */
public record SubscriptionView(ManagerStoreId store, SubscriptionStatus status, String planCode,
                               String planDisplayName, PlanPriceId planPriceId, Money amount,
                               Instant currentPeriodEnd, Instant trialEnd, boolean cancelAtPeriodEnd,
                               Instant graceUntil, PendingPlanChangeView pendingPlanChange, boolean providerLinked,
                               Map<EntitlementKey, EntitlementValue> entitlements) implements Serializable {
}
