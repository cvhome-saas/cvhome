package com.asrevo.cvhome.commons.command;

import java.util.List;
import org.springframework.cloud.stream.function.StreamBridge;

public class StreamCommandPublisher implements CommandPublisher {
    private final StreamBridge streamBridge;

    public StreamCommandPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Override
    public <T extends Command> void publish(T command) {
        List<String> destinations = this.getCommandDestination(command);
        if (destinations != null && !destinations.isEmpty()) {
            destinations.forEach(it -> this.streamBridge.send(it, command));
        }
    }

    @Override
    public <T extends Command> List<String> getCommandDestination(T command) {
        return command.destinations();
    }
}
