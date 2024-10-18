package com.asrevo.cvhome.commons.command;

import java.util.List;
import org.springframework.scheduling.annotation.Async;

public interface CommandPublisher {
    @Async
    <T extends Command> void publish(T command);

    <T extends Command> List<String> getCommandDestination(T command);
}
