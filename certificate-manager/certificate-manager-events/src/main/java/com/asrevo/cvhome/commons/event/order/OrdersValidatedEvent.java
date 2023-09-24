package com.asrevo.cvhome.commons.event.order;

import com.asrevo.cvhome.commons.domain.CertificateOrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class OrdersValidatedEvent extends OrdersEvent {
    private CertificateOrderStatus certificateOrderStatus;
    private Instant validatedDate;

    public static OrdersValidatedEvent from(Instant validatedDate, CertificateOrderStatus orderStatus) {
        OrdersValidatedEvent event = new OrdersValidatedEvent();
        event.setValidatedDate(validatedDate);
        event.setCertificateOrderStatus(orderStatus);
        return event;
    }

    @Override
    public String eventType() {
        return "OrderValidatedEvent";
    }
}
