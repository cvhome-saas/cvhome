package com.asrevo.cvhome.commons.event.domain;

import com.asrevo.cvhome.commons.domain.Domain;

public class DomainRegisteredEvent extends DomainEvent {
    public static DomainRegisteredEvent from(Domain domain) {
        DomainRegisteredEvent event = new DomainRegisteredEvent();
        event.setDomain(domain);
        return event;
    }

    @Override
    public String eventType() {
        return "DomainRegistered";
    }
}
