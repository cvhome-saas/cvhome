package com.asrevo.cvhome.billing.repository.projection;

import java.time.Instant;

import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/** One row of the platform's subscription register, joined to the plan and price it names. */
@SuppressWarnings("java:S107")
public record PlatformSubscriptionRow(StoreMerchantId store, ManagerOrgId org, SubscriptionStatus status,
                                      String planCode, String planDisplayName, CurrencyCode currency,
                                      Long unitAmount, Instant currentPeriodEnd, Instant trialEnd,
                                      Instant graceUntil, Instant suspendedAt, Instant canceledAt,
                                      boolean cancelAtPeriodEnd, boolean providerLinked, Instant createdDate) {
}
