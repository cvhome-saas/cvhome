package com.asrevo.cvhome.manager.commons.event.store;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.event.Event;
import com.asrevo.cvhome.commons.event.EventId;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
})
public sealed interface StoreEvent extends Event permits StoreCreatedEvent {
    EventId eventId();

    ManagerStoreId storeId();

    @Override
    default List<String> getDestinations() {
        return List.of("outDomainEvents-out-0");
    }
}

