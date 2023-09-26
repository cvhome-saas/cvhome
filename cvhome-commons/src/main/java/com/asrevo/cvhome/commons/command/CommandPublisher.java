package com.asrevo.cvhome.commons.command;

import java.util.List;

public interface CommandPublisher {
    <T extends Command> void publish(T command);

    <T extends Command> List<String> getCommandDestination(T command);
}
