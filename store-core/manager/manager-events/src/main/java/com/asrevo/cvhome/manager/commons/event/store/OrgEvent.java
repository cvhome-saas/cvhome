package com.asrevo.cvhome.manager.commons.event.store;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.event.Event;
import java.util.List;

public sealed interface OrgEvent extends Event
        permits OrgCreatedEvent, OrgSubscriptionPlanChangedEvent {

    ManagerOrgId org();

    @Override
    default List<String> getDestinations() {
        return List.of("events-out-0");
    }
}
