package com.asrevo.cvhome.domaincertificatemanager.commons.event.order;

import com.asrevo.cvhome.commons.event.EventId;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.OrdersId;

import java.util.Map;

public record OrdersValidationRequestedEvent(EventId eventId, OrdersId ordersId, String eventType,
                                             Map<String, String> data) implements OrdersEvent {

    public static OrdersValidationRequestedEvent from(OrdersId orderId) {
        return new OrdersValidationRequestedEvent(EventId.newId(), orderId, "OrdersValidationRequestedEvent", Map.of());
    }

    @Override
    public String eventType() {
        return "OrdersValidationRequestedEvent";
    }
}
