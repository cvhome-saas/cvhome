package com.asrevo.cvhome.certificatemanager.entity;

import com.asrevo.cvhome.commons.event.Event;
import com.asrevo.cvhome.commons.event.EventId;
import com.asrevo.cvhome.commons.event.order.OrdersEvent;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("orders_event")
public record OrdersEventsEntity(@Id EventId eventId, String eventType, Event data) {
    public static OrdersEventsEntity from(OrdersEvent data) {
        return new OrdersEventsEntity(data.eventId(), data.eventType(), data);
    }
}
