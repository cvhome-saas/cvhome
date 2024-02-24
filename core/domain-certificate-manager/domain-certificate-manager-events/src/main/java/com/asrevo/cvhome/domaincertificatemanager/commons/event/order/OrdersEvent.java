package com.asrevo.cvhome.domaincertificatemanager.commons.event.order;

import com.asrevo.cvhome.commons.event.Event;
import com.asrevo.cvhome.commons.event.EventId;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.OrdersId;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.data.annotation.Transient;

import java.util.List;
import java.util.Map;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
        @Type(value = DnsOrdersRequestedEvent.class),
        @Type(value = OrdersCertificateGeneratedEvent.class),
        @Type(value = OrdersChallengeValidationTypeChangedEvent.class),
        @Type(value = OrdersCreatedEvent.class),
        @Type(value = OrdersRequestedEvent.class),
        @Type(value = OrdersValidatedEvent.class),
        @Type(value = OrdersValidationRequestedEvent.class),
})
public sealed interface OrdersEvent extends Event permits DnsOrdersRequestedEvent, OrdersCertificateGeneratedEvent, OrdersChallengeValidationTypeChangedEvent, OrdersCreatedEvent, OrdersRequestedEvent, OrdersValidatedEvent, OrdersValidationRequestedEvent {
    EventId eventId();

    OrdersId ordersId();

    @Override
    String eventType();

    @Override
    Map<String, String> data();

    @Transient
    @Override
    default List<String> getDestinations() {
        return List.of("outOrderEvents-out-0");
    }
}
