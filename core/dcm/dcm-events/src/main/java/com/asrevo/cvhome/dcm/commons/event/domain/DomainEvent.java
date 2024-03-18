package com.asrevo.cvhome.dcm.commons.event.domain;

import com.asrevo.cvhome.commons.event.Event;
import com.asrevo.cvhome.commons.event.EventId;
import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.DomainId;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;


@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DomainRegisteredEvent.class),
        @JsonSubTypes.Type(value = DomainReferenceChangedEvent.class),
})
public sealed interface DomainEvent extends Event permits DomainReferenceChangedEvent, DomainRegisteredEvent {
    EventId eventId();

    DomainId domainId();

    Domain domain();

    @Override
    default List<String> getDestinations() {
        return List.of("outDomainEvents-out-0");
    }
}

