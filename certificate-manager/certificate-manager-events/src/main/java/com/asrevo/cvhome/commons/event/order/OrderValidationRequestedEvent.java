package com.asrevo.cvhome.commons.event.order;

public class OrderValidationRequestedEvent extends OrderEvent {
    public static OrderValidationRequestedEvent from(Long orderId) {
        OrderValidationRequestedEvent event = new OrderValidationRequestedEvent();
        event.setId(orderId);
        return event;
    }

    @Override
    public String eventType() {
        return "OrderValidationRequestedEvent";
    }
}
