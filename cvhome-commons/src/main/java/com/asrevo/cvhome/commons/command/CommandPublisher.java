package com.asrevo.cvhome.commons.command;

import org.springframework.scheduling.annotation.Async;

import java.util.List;

public interface CommandPublisher {
    @Async
    <T extends Command> void publish(T command);

    <T extends Command> List<String> getCommandDestination(T command);
}
