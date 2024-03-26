package com.asrevo.cvhome.commons.command;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

@AllArgsConstructor
@Slf4j
public class LocalCommandPublisher implements CommandPublisher {
    private final ApplicationEventPublisher publisher;

    @Override
    public <T extends Command> void publish(T command) {
        publisher.publishEvent(command);
    }

    @Override
    public <T extends Command> List<String> getCommandDestination(T command) {
        return command.destinations();
    }
}
