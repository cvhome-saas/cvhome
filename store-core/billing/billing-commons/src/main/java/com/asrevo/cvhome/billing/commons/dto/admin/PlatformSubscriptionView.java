package com.asrevo.cvhome.billing.commons.dto.admin;

import java.io.Serializable;
import java.time.Instant;

import com.asrevo.cvhome.billing.commons.Money;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * One subscription as the platform register lists it.
 *
 * <p>
 * Deliberately <em>not</em> {@code SubscriptionView}: that one carries the entitlement map and is built through the
 * uncached plan catalogue, which a paged listing cannot afford. It also lacks the three dates that make a blocked
 * row mean something — a {@code PAST_DUE} row says nothing without {@code graceUntil}, and "blocked since" is
 * {@code suspendedAt} or {@code canceledAt}.
 * </p>
 *
 * @param providerLinked whether a provider subscription stands behind the row, and so whether changing plan,
 *                       cancelling or resuming is possible at all — a trial we granted ourselves has none, and
 *                       {@code SubscriptionServiceImpl.requireProviderSubscription} refuses every lever for it.
 *                       Status does not answer this
 */
public record PlatformSubscriptionView(StoreMerchantId store, ManagerOrgId org, SubscriptionStatus status,
                                       String planCode, String planDisplayName, Money amount,
                                       Instant currentPeriodEnd, Instant trialEnd, Instant graceUntil,
                                       Instant suspendedAt, Instant canceledAt, boolean cancelAtPeriodEnd,
                                       boolean providerLinked, Instant createdDate) implements Serializable {
}
