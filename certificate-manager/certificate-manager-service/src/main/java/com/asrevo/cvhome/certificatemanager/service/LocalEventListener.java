package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.commons.event.domain.DomainEvent;
import com.asrevo.cvhome.commons.event.order.OrdersEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
@Slf4j
@AllArgsConstructor
public class LocalEventListener {
    private final StreamBridge streamBridge;

    @TransactionalEventListener
    void onOrdersEvents(OrdersEvent event) {
        log.info(event.eventType());
        streamBridge.send("logOrderEvents-out-0", event);
    }

    @TransactionalEventListener
    void onOrdersEvents(DomainEvent event) {
        log.info(event.eventType());
    }
}

