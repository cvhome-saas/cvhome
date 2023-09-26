package com.asrevo.cvhome.commons.command;

import java.util.List;

public interface CommandProcessor {
    void process(Command command);

    List<CommandImpl<Command>> getProcessors(Command command);

    List<CommandImpl<Command>> getProcessors(Class<? extends Command> tClass);

    List<CommandImpl<Command>> getAllProcessors();

}
