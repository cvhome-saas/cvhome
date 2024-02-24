package com.asrevo.cvhome.store.commons.event.store;

import com.asrevo.cvhome.commons.event.Event;
import com.asrevo.cvhome.commons.event.EventId;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
})
public sealed interface StoreEvent extends Event permits StoreCreatedEvent {
    EventId eventId();

    StoreId storeId();

    @Override
    default List<String> getDestinations() {
        return List.of("outDomainEvents-out-0");
    }
}

