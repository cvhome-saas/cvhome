package com.asrevo.cvhome.certificatemanager.entity;

import com.asrevo.cvhome.commons.domain.OrdersEventsId;
import com.asrevo.cvhome.commons.event.Event;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("orders_events")
public record OrdersEventsEntity(@Id OrdersEventsId id, String eventType, Event<?> event) {
    public static OrdersEventsEntity from(Event<?> event) {
        return new OrdersEventsEntity(OrdersEventsId.newId(), event.eventType(), event);
    }
}
