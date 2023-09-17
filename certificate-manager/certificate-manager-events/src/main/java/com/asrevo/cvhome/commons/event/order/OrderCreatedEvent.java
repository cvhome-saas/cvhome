package com.asrevo.cvhome.commons.event.order;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class OrderCreatedEvent extends OrderEvent {
    private Instant createdDate;

    public static OrderCreatedEvent from(Instant createdDate) {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setCreatedDate(createdDate);
        return event;
    }

    @Override
    public String eventType() {
        return "OrderCreatedEvent";
    }

}
