package com.asrevo.cvhome.commons.event.order;

import com.asrevo.cvhome.commons.domain.OrdersId;

public class OrderValidationRequestedEvent extends OrderEvent {
    public static OrderValidationRequestedEvent from() {
        return new OrderValidationRequestedEvent();
    }

    public static OrderValidationRequestedEvent from(OrdersId orderId) {
        OrderValidationRequestedEvent event = new OrderValidationRequestedEvent();
        event.setId(orderId);
        return event;
    }

    @Override
    public String eventType() {
        return "OrderValidationRequestedEvent";
    }
}
