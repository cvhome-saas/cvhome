package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.commons.event.domain.DomainEvent;
import com.asrevo.cvhome.commons.event.order.OrdersEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@AllArgsConstructor
public class LocalEventListener {
    private final StreamBridge streamBridge;

    // @TODO replace with @TransactionalEventListener as its not working fine now
    @EventListener
    void onOrdersEvents(OrdersEvent event) {
        log.info(event.eventType());
        streamBridge.send("logOrderEvents-out-0", event);
    }

    @EventListener
    void onDomainEvents(DomainEvent event) {
        log.info(event.eventType());
    }
}

