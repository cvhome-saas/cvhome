package com.asrevo.cvhome.commons.event.order;

import com.asrevo.cvhome.commons.domain.CertificateOrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@Getter
@Setter
public class OrdersCertificateGeneratedEvent extends OrdersEvent {
    private Instant generatedDate;
    private CertificateOrderStatus certificateOrderStatus;

    public static OrdersCertificateGeneratedEvent from(CertificateOrderStatus orderStatus, Instant generatedDate) {
        OrdersCertificateGeneratedEvent event = new OrdersCertificateGeneratedEvent();
        event.setCertificateOrderStatus(orderStatus);
        event.setGeneratedDate(generatedDate);
        return event;
    }

    @Override
    public String eventType() {
        return "OrderCertificateGeneratedEvent";
    }
}
