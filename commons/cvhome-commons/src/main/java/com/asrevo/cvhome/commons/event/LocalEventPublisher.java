package com.asrevo.cvhome.commons.event;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

@AllArgsConstructor
@Slf4j
public class LocalEventPublisher implements EventPublisher {
    private final ApplicationEventPublisher publisher;

    @Override
    public <T extends Event> void publish(T event) {
        publisher.publishEvent(event);

    }

    @Override
    public <T extends Event> List<String> getEventDestinations(T event) {
        return event.getDestinations();
    }
}
