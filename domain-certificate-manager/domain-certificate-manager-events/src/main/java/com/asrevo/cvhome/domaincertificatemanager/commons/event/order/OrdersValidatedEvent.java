package com.asrevo.cvhome.domaincertificatemanager.commons.event.order;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.CertificateOrderStatus;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.OrdersId;
import com.asrevo.cvhome.commons.event.EventId;

import java.time.Instant;
import java.util.Map;

public record OrdersValidatedEvent(EventId eventId, OrdersId ordersId, String eventType,
                                   Map<String, String> data) implements OrdersEvent {
    private final static String DOMAIN_KEY = "domain";
    private final static String VALIDATE_DATE_KEY = "validatedDate";
    private final static String CERTIFICATE_ORDER_STATUS_KEY = "certificateOrderStatus";

    public static OrdersValidatedEvent from(OrdersId ordersId, Domain domain, Instant validatedDate, CertificateOrderStatus orderStatus) {
        return new OrdersValidatedEvent(EventId.newId(), ordersId, "OrdersValidatedEvent", Map.of(DOMAIN_KEY, domain.domain(), VALIDATE_DATE_KEY, validatedDate.toString(), CERTIFICATE_ORDER_STATUS_KEY, orderStatus.name()));
    }

    public Domain domain() {
        return new Domain(this.data.get(DOMAIN_KEY));
    }

    public CertificateOrderStatus certificateOrderStatus() {
        return CertificateOrderStatus.valueOf(this.data.get(CERTIFICATE_ORDER_STATUS_KEY));
    }

    public Instant validatedDate() {
        return Instant.parse(this.data.get(VALIDATE_DATE_KEY));
    }

    @Override
    public String eventType() {
        return "OrdersValidatedEvent";
    }
}
