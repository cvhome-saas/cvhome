package com.asrevo.cvhome.commons.command;

public interface CommandImpl<T extends Command> {
    void process(T command);

    String type();

    default Integer order() {
        return 0;
    }
}