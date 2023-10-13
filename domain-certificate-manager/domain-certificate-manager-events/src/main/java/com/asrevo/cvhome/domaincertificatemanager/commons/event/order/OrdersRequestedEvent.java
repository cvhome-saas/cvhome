package com.asrevo.cvhome.domaincertificatemanager.commons.event.order;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.OrderLocation;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.OrdersId;
import com.asrevo.cvhome.commons.event.EventId;

import java.time.Instant;
import java.util.Map;

public record OrdersRequestedEvent(EventId eventId, OrdersId ordersId, String eventType,
                                   Map<String, String> data) implements OrdersEvent {
    private final static String DOMAIN_KEY = "domain";
    private final static String ORDER_LOCATION_KEY = "location";
    private final static String REQUESTED_DATE_KEY = "requestedDate";

    public static OrdersRequestedEvent from(OrdersId ordersId, Domain domain, OrderLocation location, Instant requestedDate) {
        return new OrdersRequestedEvent(EventId.newId(), ordersId, "OrderRequestedEvent", Map.of(DOMAIN_KEY, domain.domain(), ORDER_LOCATION_KEY, location.location(), REQUESTED_DATE_KEY, requestedDate.toString()));
    }

    public Domain domain() {
        return new Domain(this.data.get(DOMAIN_KEY));
    }

    public OrderLocation location() {
        return new OrderLocation(this.data.get(ORDER_LOCATION_KEY));
    }

    public Instant requestedDate() {
        return Instant.parse(this.data.get(REQUESTED_DATE_KEY));
    }

    @Override
    public String eventType() {
        return "OrderRequestedEvent";
    }
}
