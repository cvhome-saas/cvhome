package com.asrevo.cvhome.controlplane.manager.commons.event.store;

import java.util.List;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.event.Event;

public sealed interface OrgEvent extends Event permits OrgCreatedEvent {

    ManagerOrgId org();

    @Override
    default List<String> getDestinations() {
        return List.of("events-out-0");
    }

}
