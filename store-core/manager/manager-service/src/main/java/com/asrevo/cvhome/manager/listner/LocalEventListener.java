package com.asrevo.cvhome.manager.listner;

import com.asrevo.cvhome.commons.event.Event;
import com.asrevo.cvhome.commons.event.EventProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocalEventListener {
    private final EventProcessor eventProcessor;

    @TransactionalEventListener
    void on(Event event) {
        log.info("firing event {}", event);
        eventProcessor.process(event);
    }
}
