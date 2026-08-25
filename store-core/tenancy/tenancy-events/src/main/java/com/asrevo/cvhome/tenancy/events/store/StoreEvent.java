package com.asrevo.cvhome.tenancy.events.store;

import java.util.List;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.event.Event;

public sealed interface StoreEvent extends Event permits StoreCreatedEvent {

    StoreMerchantId store();

    @Override
    default List<String> getDestinations() {
        return List.of("events-out-0");
    }

}
