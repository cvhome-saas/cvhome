package com.asrevo.cvhome.commons.event.order;

import com.asrevo.cvhome.commons.domain.CertificateOrderStatus;
import com.asrevo.cvhome.commons.domain.DomainCertificateOrder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@Getter
@Setter
public class OrderCertificateGeneratedEvent extends OrderEvent {
    private Instant generatedDate;
    private CertificateOrderStatus certificateOrderStatus;

    public static OrderCertificateGeneratedEvent from(DomainCertificateOrder order) {
        OrderCertificateGeneratedEvent event = new OrderCertificateGeneratedEvent();
        event.setCertificateOrderStatus(order.getCertificateOrderStatus());
        event.setGeneratedDate(order.getGeneratedDate());
        return event;
    }

    @Override
    public String eventType() {
        return "OrderCertificateGeneratedEvent";
    }
}
