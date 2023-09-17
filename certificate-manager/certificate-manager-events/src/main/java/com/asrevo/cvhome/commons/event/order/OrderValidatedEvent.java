package com.asrevo.cvhome.commons.event.order;

import com.asrevo.cvhome.commons.domain.CertificateOrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class OrderValidatedEvent extends OrderEvent {
    private CertificateOrderStatus certificateOrderStatus;
    private Instant validatedDate;

    public static OrderValidatedEvent from(Instant validatedDate, CertificateOrderStatus orderStatus) {
        OrderValidatedEvent event = new OrderValidatedEvent();
        event.setValidatedDate(validatedDate);
        event.setCertificateOrderStatus(orderStatus);
        return event;
    }

    @Override
    public String eventType() {
        return "OrderValidatedEvent";
    }
}
