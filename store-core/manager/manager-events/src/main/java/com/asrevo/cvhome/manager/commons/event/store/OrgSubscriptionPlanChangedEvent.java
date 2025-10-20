package com.asrevo.cvhome.manager.commons.event.store;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.SubscriptionPlan;
import java.util.Map;

public record OrgSubscriptionPlanChangedEvent(ManagerOrgId org, Map<String, String> data) implements OrgEvent {
	public static OrgSubscriptionPlanChangedEvent from(ManagerOrgId org, SubscriptionPlan newSubscriptionPlan,
			SubscriptionPlan oldSubscriptionPlan) {
		return new OrgSubscriptionPlanChangedEvent(org, Map.of("newSubscriptionPlan", newSubscriptionPlan.name(),
				"oldSubscriptionPlan", oldSubscriptionPlan.name()));
	}

	@Override
	public String eventType() {
		return OrgSubscriptionPlanChangedEvent.class.getSimpleName();
	}
}
