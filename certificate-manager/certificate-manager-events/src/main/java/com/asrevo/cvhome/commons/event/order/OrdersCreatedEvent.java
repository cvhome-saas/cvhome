package com.asrevo.cvhome.commons.event.order;

import com.asrevo.cvhome.commons.domain.OrdersId;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class OrdersCreatedEvent extends OrdersEvent {
    private Instant createdDate;

    public static OrdersCreatedEvent from(OrdersId id, Instant createdDate) {
        OrdersCreatedEvent event = new OrdersCreatedEvent();
        event.setId(id);
        event.setCreatedDate(createdDate);
        return event;
    }

    @Override
    public String eventType() {
        return "OrderCreatedEvent";
    }

}
