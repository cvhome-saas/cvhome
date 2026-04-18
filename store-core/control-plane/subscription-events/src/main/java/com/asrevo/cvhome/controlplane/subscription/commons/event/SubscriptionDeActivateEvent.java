package com.asrevo.cvhome.controlplane.subscription.commons.event;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;

public record SubscriptionDeActivateEvent(ManagerOrgId orgId, Map<String, String> data) implements SubscriptionEvent {

    public static SubscriptionDeActivateEvent from(ManagerOrgId orgId) {
        return new SubscriptionDeActivateEvent(orgId, Map.of());
    }

    @Override
    public String eventType() {
        return SubscriptionDeActivateEvent.class.getSimpleName();
    }
}
