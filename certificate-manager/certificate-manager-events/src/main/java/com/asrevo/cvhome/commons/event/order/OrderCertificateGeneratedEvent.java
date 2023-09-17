package com.asrevo.cvhome.commons.event.order;

import com.asrevo.cvhome.commons.domain.CertificateOrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@Getter
@Setter
public class OrderCertificateGeneratedEvent extends OrderEvent {
    private Instant generatedDate;
    private CertificateOrderStatus certificateOrderStatus;

    public static OrderCertificateGeneratedEvent from(CertificateOrderStatus orderStatus, Instant generatedDate) {
        OrderCertificateGeneratedEvent event = new OrderCertificateGeneratedEvent();
        event.setCertificateOrderStatus(orderStatus);
        event.setGeneratedDate(generatedDate);
        return event;
    }

    @Override
    public String eventType() {
        return "OrderCertificateGeneratedEvent";
    }
}
