package com.asrevo.cvhome.certificatemanager.commons.event.order;

import com.asrevo.cvhome.certificatemanager.commons.domain.OrdersId;
import com.asrevo.cvhome.commons.event.EventId;

import java.time.Instant;
import java.util.Map;

public record OrdersCreatedEvent(EventId eventId, OrdersId ordersId, String eventType,
                                 Map<String, String> data) implements OrdersEvent {
    public final static String CREATED_DATE_KEY = "createdDate";


    public static OrdersCreatedEvent from(OrdersId ordersId, Instant createdDate) {
        return new OrdersCreatedEvent(EventId.newId(), ordersId, "OrdersCreatedEvent", Map.of(CREATED_DATE_KEY, createdDate.toString()));
    }

    Instant createdDate() {
        return Instant.parse(this.data.get(CREATED_DATE_KEY));
    }

    @Override
    public String eventType() {
        return "OrdersCreatedEvent";
    }

}
