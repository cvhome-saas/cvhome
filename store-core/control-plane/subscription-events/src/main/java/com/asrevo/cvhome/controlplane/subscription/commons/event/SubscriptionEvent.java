package com.asrevo.cvhome.controlplane.subscription.commons.event;

import java.util.List;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.event.Event;

public sealed interface SubscriptionEvent extends Event
        permits SubscriptionDeActivateEvent, SubscriptionActivateEvent, SubscriptionCreatedEvent {

    ManagerOrgId orgId();

    @Override
    default List<String> getDestinations() {
        return List.of("events-out-0");
    }

}
