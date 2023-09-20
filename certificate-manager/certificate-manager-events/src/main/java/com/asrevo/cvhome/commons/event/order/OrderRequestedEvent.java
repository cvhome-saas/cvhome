package com.asrevo.cvhome.commons.event.order;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.OrderLocation;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class OrderRequestedEvent extends OrderEvent {
    private Domain domain;
    private OrderLocation location;
    private Instant requestedDate;

    public static OrderRequestedEvent from(Domain domain, OrderLocation location, Instant requestedDate) {
        OrderRequestedEvent event = new OrderRequestedEvent();
        event.setDomain(domain);
        event.setLocation(location);
        event.setRequestedDate(requestedDate);
        return event;
    }

    @Override
    public String eventType() {
        return "OrderRequestedEvent";
    }
}
