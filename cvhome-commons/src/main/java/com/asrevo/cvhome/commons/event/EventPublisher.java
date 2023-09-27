package com.asrevo.cvhome.commons.event;


import java.util.List;

public interface EventPublisher {
    <T extends Event> void publish(T event);

    <T extends Event> List<String> getEventDestinations(T event);
}
