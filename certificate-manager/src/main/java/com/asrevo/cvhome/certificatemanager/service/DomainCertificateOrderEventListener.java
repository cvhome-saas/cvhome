package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.commons.event.Event;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
@Slf4j
@AllArgsConstructor
public class DomainCertificateOrderEventListener {
    private final StreamBridge streamBridge;

    @TransactionalEventListener
    void onDomainCertificateDomainCertificateOrderCreated(Event event) {
        log.info(event.eventType());
        streamBridge.send("logOrderEvents-out-0", event);
    }
}

