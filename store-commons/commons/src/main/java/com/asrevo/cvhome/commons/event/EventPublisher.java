package com.asrevo.cvhome.commons.event;

import java.util.List;

import org.springframework.scheduling.annotation.Async;

public interface EventPublisher {

    @Async
    <T extends Event> void publish(T event);

    <T extends Event> List<String> getEventDestinations(T event);

}
