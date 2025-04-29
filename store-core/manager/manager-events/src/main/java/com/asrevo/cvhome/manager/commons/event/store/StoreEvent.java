package com.asrevo.cvhome.manager.commons.event.store;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.event.Event;
import java.util.List;

public sealed interface StoreEvent extends Event permits StoreCreatedEvent, StoreProvisionedEvent {

    ManagerStoreId store();

    @Override
    default List<String> getDestinations() {
        return List.of("events-out-0");
    }
}
