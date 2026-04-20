package com.asrevo.cvhome.controlplane.subscription.commons;

import java.time.Instant;

import com.asrevo.cvhome.commons.domain.SubscriptionPlan;

public record SubscriptionPlanDetails(Instant createdDate, Instant lastRenewedDate, Instant endDate,
                                      Instant deActivatedDate, SubscriptionPlan subscriptionPlan, RecurringPlan recurringPlan,
                                      SubscriptionStatus status) {
}
