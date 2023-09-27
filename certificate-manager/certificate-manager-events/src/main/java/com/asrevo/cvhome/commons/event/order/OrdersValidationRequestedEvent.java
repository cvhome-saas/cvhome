package com.asrevo.cvhome.commons.event.order;

import com.asrevo.cvhome.commons.domain.OrdersId;
import com.asrevo.cvhome.commons.event.EventId;

import java.util.Map;

public record OrdersValidationRequestedEvent(EventId eventId, OrdersId ordersId, String eventType,
                                             Map<String, String> data) implements OrdersEvent {

    public static OrdersValidationRequestedEvent from(OrdersId orderId) {
        return new OrdersValidationRequestedEvent(EventId.newId(), orderId, "OrderValidationRequestedEvent", Map.of());
    }

    @Override
    public String eventType() {
        return "OrderValidationRequestedEvent";
    }
}
