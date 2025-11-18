package com.asrevo.cvhome.controlplane.subscription.commons.event;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import java.util.Map;

public record SubscriptionActivateEvent(ManagerOrgId orgId, Map<String, String> data) implements SubscriptionEvent {

	public static SubscriptionActivateEvent from(ManagerOrgId orgId) {
		return new SubscriptionActivateEvent(orgId, Map.of());
	}

	@Override
	public String eventType() {
		return SubscriptionActivateEvent.class.getSimpleName();
	}
}
