package com.asrevo.cvhome.controlplane.subscription.commons;

import com.asrevo.cvhome.commons.domain.SubscriptionPlan;
import java.time.Instant;

public record SubscriptionPlanDetails(Instant createdDate, Instant lastRenewedDate, Instant endDate,
		Instant deActivatedDate, SubscriptionPlan subscriptionPlan, RecurringPlan recurringPlan,
		SubscriptionStatus status) {
}
