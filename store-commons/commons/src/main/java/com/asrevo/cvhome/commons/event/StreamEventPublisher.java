package com.asrevo.cvhome.commons.event;

import java.util.List;

import org.springframework.cloud.stream.function.StreamBridge;

public class StreamEventPublisher implements EventPublisher {

    private final StreamBridge streamBridge;

    public StreamEventPublisher(StreamBridge streamBridge) {
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
