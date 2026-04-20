package com.asrevo.cvhome.controlplane.subscription.commons.event;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;

public record SubscriptionActivateEvent(ManagerOrgId orgId, Map<String, String> data) implements SubscriptionEvent {

    public static SubscriptionActivateEvent from(ManagerOrgId orgId) {
        return new SubscriptionActivateEvent(orgId, Map.of());
    }

    @Override
    public String eventType() {
        return SubscriptionActivateEvent.class.getSimpleName();
    }
}
