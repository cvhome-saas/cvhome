package com.asrevo.cvhome.subscription.commons.command;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import java.util.Map;

public record DeActivateNonRenewedSubscriptionCommand(ManagerOrgId org,
		Map<String, String> data) implements SubscriptionCommand {
	public static DeActivateNonRenewedSubscriptionCommand from(final ManagerOrgId org) {
		return new DeActivateNonRenewedSubscriptionCommand(org, Map.of());
	}

	@Override
	public String eventType() {
		return DeActivateNonRenewedSubscriptionCommand.class.getSimpleName();
	}
}
