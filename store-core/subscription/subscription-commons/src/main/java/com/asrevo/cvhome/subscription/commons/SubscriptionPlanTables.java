package com.asrevo.cvhome.subscription.commons;

import java.util.Map;

public record SubscriptionPlanTables(Map<RecurringPlan, SubscriptionPlanTable> tables,
		SubscriptionPlanOption freeOption) {
}
