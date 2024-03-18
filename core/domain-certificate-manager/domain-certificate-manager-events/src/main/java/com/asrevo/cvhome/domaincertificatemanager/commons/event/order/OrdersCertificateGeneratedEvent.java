package com.asrevo.cvhome.domaincertificatemanager.commons.event.order;

import com.asrevo.cvhome.commons.event.EventId;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.CertificateOrderStatus;
import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.OrdersId;

import java.time.Instant;
import java.util.Map;


public record OrdersCertificateGeneratedEvent(EventId eventId, OrdersId ordersId, String eventType,
                                              Map<String, String> data) implements OrdersEvent {
    private static final String DOMAIN_KEY = "domain";
    private static final String CERTIFICATE_ORDER_STATUS_KEY = "certificateOrderStatus";
    private static final String GENERATED_DATE_KEY = "generatedDate";

    public static OrdersCertificateGeneratedEvent from(OrdersId ordersId, Domain domain, CertificateOrderStatus orderStatus, Instant generatedDate) {
        return new OrdersCertificateGeneratedEvent(EventId.newId(), ordersId, "OrderCertificateGeneratedEvent", Map.of(DOMAIN_KEY, domain.domain(), CERTIFICATE_ORDER_STATUS_KEY, orderStatus.name(), GENERATED_DATE_KEY, generatedDate.toString()));
    }

    public Domain domain() {
        return new Domain(this.data.get(DOMAIN_KEY));
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
