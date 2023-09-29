package com.asrevo.cvhome.commons.command;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultCommandProcessor implements CommandProcessor {
    private final List<CommandImpl<Command>> commandsImpl;

    public DefaultCommandProcessor(List<CommandImpl<Command>> commandsImpl) {
        this.commandsImpl = commandsImpl;
    }

    @Override
    public void process(Command command) {
        getProcessors(command.getClass()).forEach(it -> it.process(command));
    }

    @Override
    public List<CommandImpl<Command>> getProcessors(Command command) {
        if (command == null) {
            return List.of();
        }
        return getProcessors(command.getClass());
    }

    @Override
    public List<CommandImpl<Command>> getProcessors(Class<? extends Command> tClass) {
        // @formatter:off
        return commandsImpl.stream()
                .filter(it -> it.type().equals(tClass.getName()))
                .sorted(Comparator.comparing(CommandImpl::order))
                .collect(Collectors.toList());
        // @formatter:on
    }


    @Override
    public List<CommandImpl<Command>> getAllProcessors() {
        return commandsImpl;
    }
}
