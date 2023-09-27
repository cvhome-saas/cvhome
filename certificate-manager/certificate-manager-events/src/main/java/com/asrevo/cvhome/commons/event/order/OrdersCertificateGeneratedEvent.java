package com.asrevo.cvhome.commons.event.order;

import com.asrevo.cvhome.commons.domain.CertificateOrderStatus;
import com.asrevo.cvhome.commons.domain.OrdersId;
import com.asrevo.cvhome.commons.event.EventId;

import java.time.Instant;
import java.util.Map;


public record OrdersCertificateGeneratedEvent(EventId eventId, OrdersId ordersId, String eventType,
                                              Map<String, String> data) implements OrdersEvent {
    private static final String CERTIFICATE_ORDER_STATUS_KEY = "certificateOrderStatus";
    private static final String GENERATED_DATE_KEY = "generatedDate";

    public static OrdersCertificateGeneratedEvent from(OrdersId ordersId, CertificateOrderStatus orderStatus, Instant generatedDate) {
        return new OrdersCertificateGeneratedEvent(EventId.newId(), ordersId, "OrderCertificateGeneratedEvent", Map.of(CERTIFICATE_ORDER_STATUS_KEY, orderStatus.name(), GENERATED_DATE_KEY, generatedDate.toString()));
    }

    public CertificateOrderStatus certificateOrderStatus() {
        return CertificateOrderStatus.valueOf(this.data.get(CERTIFICATE_ORDER_STATUS_KEY));
    }

    public Instant generatedDate() {
        return Instant.parse(this.data.get(GENERATED_DATE_KEY));
    }


    @Override
    public String eventType() {
        return "OrderCertificateGeneratedEvent";
    }
}
