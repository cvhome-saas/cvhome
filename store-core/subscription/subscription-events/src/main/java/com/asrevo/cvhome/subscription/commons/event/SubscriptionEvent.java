package com.asrevo.cvhome.subscription.commons.event;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.event.Event;
import java.util.List;

public sealed interface SubscriptionEvent extends Event
        permits SubscriptionDeActivateEvent, SubscriptionActivateEvent, SubscriptionCreatedEvent {
    ManagerOrgId orgId();

    @Override
    default List<String> getDestinations() {
        return List.of("events-out-0");
    }
}
