package com.asrevo.cvhome.domaincertificatemanager.commons.event.order;

import com.asrevo.cvhome.commons.event.EventId;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.OrderLocation;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.OrdersId;

import java.time.Instant;
import java.util.AbstractMap;
import java.util.Map;

public record DnsOrdersRequestedEvent(EventId eventId, OrdersId ordersId, String eventType,
                                      Map<String, String> data) implements OrdersEvent {
    private final static String DOMAIN_KEY = "domain";
    private final static String ORDER_LOCATION_KEY = "location";
    private final static String REQUESTED_DATE_KEY = "requestedDate";
    private final static String DNS_CHALLENGE_KEY_KEY = "dnsKey";

    private final static String DNS_CHALLENGE_VALUE_KEY = "dnsValue";

    public static DnsOrdersRequestedEvent from(OrdersId ordersId, Domain domain, OrderLocation location, Instant requestedDate, String dnsKey, String dnsValue) {
        return new DnsOrdersRequestedEvent(EventId.newId(), ordersId, "OrderRequestedEvent", Map.of(
                DNS_CHALLENGE_KEY_KEY, dnsKey,
                DNS_CHALLENGE_VALUE_KEY, dnsValue,
                DOMAIN_KEY, domain.domain(),
                ORDER_LOCATION_KEY, location.location(),
                REQUESTED_DATE_KEY, requestedDate.toString()));
    }

    public Map.Entry<String, String> dns01Challenge() {
        return new AbstractMap.SimpleEntry<>(this.data.get(DNS_CHALLENGE_KEY_KEY), this.data.get(DNS_CHALLENGE_VALUE_KEY));
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
        return "DnsOrdersRequestedEvent";
    }
}
