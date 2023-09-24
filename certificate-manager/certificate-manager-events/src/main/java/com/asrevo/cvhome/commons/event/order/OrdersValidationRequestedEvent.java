package com.asrevo.cvhome.commons.event.order;

import com.asrevo.cvhome.commons.domain.OrdersId;

public class OrdersValidationRequestedEvent extends OrdersEvent {
    public static OrdersValidationRequestedEvent from() {
        return new OrdersValidationRequestedEvent();
    }

    public static OrdersValidationRequestedEvent from(OrdersId orderId) {
        OrdersValidationRequestedEvent event = new OrdersValidationRequestedEvent();
        event.setId(orderId);
        return event;
    }

    @Override
    public String eventType() {
        return "OrderValidationRequestedEvent";
    }
}
