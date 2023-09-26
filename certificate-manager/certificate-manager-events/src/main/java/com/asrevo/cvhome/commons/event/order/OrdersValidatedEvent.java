package com.asrevo.cvhome.commons.event.order;

import com.asrevo.cvhome.commons.domain.CertificateOrderStatus;
import com.asrevo.cvhome.commons.domain.OrdersId;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class OrdersValidatedEvent extends OrdersEvent {
    private CertificateOrderStatus certificateOrderStatus;
    private Instant validatedDate;

    public static OrdersValidatedEvent from(OrdersId id, Instant validatedDate, CertificateOrderStatus orderStatus) {
        OrdersValidatedEvent event = new OrdersValidatedEvent();
        event.setId(id);
        event.setValidatedDate(validatedDate);
        event.setCertificateOrderStatus(orderStatus);
        return event;
    }

    @Override
    public String eventType() {
        return "OrderValidatedEvent";
    }
}
