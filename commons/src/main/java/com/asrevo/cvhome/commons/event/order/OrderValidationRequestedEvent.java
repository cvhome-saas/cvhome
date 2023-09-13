package com.asrevo.cvhome.commons.event.order;

import com.asrevo.cvhome.commons.domain.DomainCertificateOrder;

public class OrderValidationRequestedEvent extends OrderEvent {
    public static OrderValidationRequestedEvent from(DomainCertificateOrder order) {
        OrderValidationRequestedEvent event = new OrderValidationRequestedEvent();
        event.setId(order.getId());
        return event;
    }

    @Override
    public String eventType() {
        return "OrderValidationRequestedEvent";
    }
}
