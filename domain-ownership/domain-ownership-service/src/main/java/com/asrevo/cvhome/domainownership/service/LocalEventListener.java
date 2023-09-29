package com.asrevo.cvhome.domainownership.service;

import com.asrevo.cvhome.commons.event.DefaultEventPublisher;
import com.asrevo.cvhome.commons.event.Event;
import com.asrevo.cvhome.commons.event.EventProcessor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@AllArgsConstructor
public class LocalEventListener {
    private final DefaultEventPublisher eventPublisher;
    private final EventProcessor eventProcessor;

    // @TODO replace with @TransactionalEventListener as its not working fine now
    @EventListener
    void onOrdersEvents(Event event) {
        log.info(event.eventType());
        eventProcessor.process(event);
        eventPublisher.publish(event);
    }
}

