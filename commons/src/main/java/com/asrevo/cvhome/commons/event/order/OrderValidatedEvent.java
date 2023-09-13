package com.asrevo.cvhome.commons.event.order;

import com.asrevo.cvhome.commons.domain.CertificateOrderStatus;
import com.asrevo.cvhome.commons.domain.DomainCertificateOrder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class OrderValidatedEvent extends OrderEvent {
    private CertificateOrderStatus certificateOrderStatus;
    private Instant validatedDate;

    public static OrderValidatedEvent from(DomainCertificateOrder order) {
        OrderValidatedEvent event = new OrderValidatedEvent();
        event.setValidatedDate(order.getValidatedDate());
        event.setCertificateOrderStatus(order.getCertificateOrderStatus());
        return event;
    }

    @Override
    public String eventType() {
        return "OrderValidatedEvent";
    }
}
