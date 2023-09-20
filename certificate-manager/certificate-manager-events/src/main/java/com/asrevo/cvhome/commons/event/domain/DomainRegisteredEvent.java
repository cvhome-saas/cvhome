package com.asrevo.cvhome.commons.event.domain;

import com.asrevo.cvhome.commons.domain.Domain;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DomainRegisteredEvent extends DomainEvent {
    private boolean autoRenew;
    private boolean autoOrder;

    public static DomainRegisteredEvent from(Domain domain, boolean autoRenew, boolean autoOrder) {
        DomainRegisteredEvent event = new DomainRegisteredEvent();
        event.setDomain(domain);
        event.setAutoRenew(autoRenew);
        event.setAutoOrder(autoOrder);
        return event;
    }

    @Override
    public String eventType() {
        return "DomainRegistered";
    }
}
