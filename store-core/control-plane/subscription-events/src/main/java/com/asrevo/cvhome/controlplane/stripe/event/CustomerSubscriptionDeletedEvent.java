package com.asrevo.cvhome.controlplane.stripe.event;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import java.util.Map;

public record CustomerSubscriptionDeletedEvent(ManagerOrgId org, Map<String, String> data) implements StripeEvent {

	public static CustomerSubscriptionDeletedEvent from(ManagerOrgId org) {
		return new CustomerSubscriptionDeletedEvent(org, Map.of());
	}

	@Override
	public Map<String, String> data() {
		return Map.of();
	}

	public String eventType() {
		return CustomerSubscriptionDeletedEvent.class.getSimpleName();
	}
}
