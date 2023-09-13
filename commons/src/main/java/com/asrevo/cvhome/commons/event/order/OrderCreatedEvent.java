package com.asrevo.cvhome.commons.event.order;

import com.asrevo.cvhome.commons.domain.DomainCertificateOrder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class OrderCreatedEvent extends OrderEvent {
    private Instant createdDate;

    public static OrderCreatedEvent from(DomainCertificateOrder domainCertificateOrder) {
        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setCreatedDate(domainCertificateOrder.getCreatedDate());
        return event;
    }

    @Override
    public String eventType() {
        return "OrderCreatedEvent";
    }

}
