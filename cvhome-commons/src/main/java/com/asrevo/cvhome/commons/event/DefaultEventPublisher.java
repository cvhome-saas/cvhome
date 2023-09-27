package com.asrevo.cvhome.commons.event;

import org.springframework.cloud.stream.function.StreamBridge;

import java.util.List;

public class DefaultEventPublisher implements EventPublisher {
    private final StreamBridge streamBridge;

    public DefaultEventPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Override
    public <T extends Event> void publish(T event) {
        List<String> destinations = this.getEventDestinations(event);
        if (destinations != null && !destinations.isEmpty()) {
            destinations.forEach(it -> this.streamBridge.send(it, event));
        }
    }

    @Override
    public <T extends Event> List<String> getEventDestinations(T event) {
        return event.getDestinations();
    }
}
