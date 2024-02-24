package com.asrevo.cvhome.commons.command;

import org.springframework.scheduling.annotation.Async;

public interface CommandImpl<T extends Command> {
    @Async
    void process(T command);

    String type();

    default Integer order() {
        return 0;
    }
}