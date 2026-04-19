package com.asrevo.cvhome.controlplane.manager.commons.event.store;

import java.util.List;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.event.Event;

public sealed interface StoreEvent extends Event permits StoreCreatedEvent, StoreProvisionedEvent {

    ManagerStoreId store();

    @Override
    default List<String> getDestinations() {
        return List.of("events-out-0");
    }

}
