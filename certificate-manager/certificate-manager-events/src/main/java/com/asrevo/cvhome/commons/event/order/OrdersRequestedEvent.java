package com.asrevo.cvhome.commons.event.order;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.OrderLocation;
import com.asrevo.cvhome.commons.domain.OrdersId;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class OrdersRequestedEvent extends OrdersEvent {
    private Domain domain;
    private OrderLocation location;
    private Instant requestedDate;

    public static OrdersRequestedEvent from(OrdersId id, Domain domain, OrderLocation location, Instant requestedDate) {
        OrdersRequestedEvent event = new OrdersRequestedEvent();
        event.setId(id);
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
