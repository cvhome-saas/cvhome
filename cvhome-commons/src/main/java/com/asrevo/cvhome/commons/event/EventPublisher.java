package com.asrevo.cvhome.commons.event;


import org.springframework.scheduling.annotation.Async;

import java.util.List;

public interface EventPublisher {
    @Async
    <T extends Event> void publish(T event);

    <T extends Event> List<String> getEventDestinations(T event);
}
