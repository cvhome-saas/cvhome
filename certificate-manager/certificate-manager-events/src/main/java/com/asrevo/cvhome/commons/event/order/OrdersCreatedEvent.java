package com.asrevo.cvhome.commons.event.order;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class OrdersCreatedEvent extends OrdersEvent {
    private Instant createdDate;

    public static OrdersCreatedEvent from(Instant createdDate) {
        OrdersCreatedEvent event = new OrdersCreatedEvent();
        event.setCreatedDate(createdDate);
        return event;
    }

    @Override
    public String eventType() {
        return "OrderCreatedEvent";
    }

}
