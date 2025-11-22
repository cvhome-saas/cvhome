package com.asrevo.cvhome.controlplane.subscription.commons.command;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.event.Event;

public sealed interface SubscriptionCommand extends Event permits DeActivateNonRenewedSubscriptionCommand {

	ManagerOrgId org();

}
