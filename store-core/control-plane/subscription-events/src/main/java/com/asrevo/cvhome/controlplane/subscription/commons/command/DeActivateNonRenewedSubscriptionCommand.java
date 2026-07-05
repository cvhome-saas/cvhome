package com.asrevo.cvhome.controlplane.subscription.commons.command;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;

import io.namastack.outbox.annotation.OutboxEvent;

@OutboxEvent(key = "#this.org().id().toString()")
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
