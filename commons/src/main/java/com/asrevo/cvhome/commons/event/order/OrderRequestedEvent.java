package com.asrevo.cvhome.commons.event.order;

import com.asrevo.cvhome.commons.domain.DomainCertificateOrder;
import com.asrevo.cvhome.commons.domain.OrderLocation;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class OrderRequestedEvent extends OrderEvent {
    private OrderLocation location;
    private Instant requestedDate;

    public static OrderRequestedEvent from(DomainCertificateOrder order) {
        OrderRequestedEvent event = new OrderRequestedEvent();
        event.setLocation(order.getLocation());
        event.setRequestedDate(order.getRequestedDate());
        return event;
    }

    @Override
    public String eventType() {
        return "OrderRequestedEvent";
    }
}
