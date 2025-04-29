package com.asrevo.cvhome.subscription.commons.event;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import java.util.Map;

public record SubscriptionDeActivateEvent(ManagerOrgId orgId, Map<String, String> data)
        implements SubscriptionEvent {

    public static SubscriptionDeActivateEvent from(ManagerOrgId orgId) {
        return new SubscriptionDeActivateEvent(orgId, Map.of());
    }

    @Override
    public String eventType() {
        return SubscriptionDeActivateEvent.class.getSimpleName();
    }
}
