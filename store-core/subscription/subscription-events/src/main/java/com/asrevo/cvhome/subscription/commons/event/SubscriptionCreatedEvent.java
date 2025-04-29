package com.asrevo.cvhome.subscription.commons.event;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.SubscriptionPlan;
import java.util.Map;

public record SubscriptionCreatedEvent(ManagerOrgId orgId, Map<String, String> data)
        implements SubscriptionEvent {

    public static SubscriptionCreatedEvent from(
            ManagerOrgId orgId, SubscriptionPlan subscriptionPlan) {
        return new SubscriptionCreatedEvent(
                orgId, Map.of("subscriptionPlan", subscriptionPlan.name()));
    }

    @Override
    public String eventType() {
        return SubscriptionCreatedEvent.class.getSimpleName();
    }
}
