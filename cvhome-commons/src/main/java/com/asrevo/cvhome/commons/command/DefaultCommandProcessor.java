package com.asrevo.cvhome.commons.command;

import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class DefaultCommandProcessor implements CommandProcessor {
    private final List<CommandImpl<Command>> commandsImpl;

    public DefaultCommandProcessor(List<CommandImpl<Command>> commandsImpl) {
        this.commandsImpl = commandsImpl;
    }

    @Override
    public void process(Command command) {
        List<CommandImpl<Command>> processors = getProcessors(command.getClass());
        processors.forEach(it -> it.process(command));
        if (processors.isEmpty()) {
            log.warn("could not find any command impl for {}", command.getClass());
        }
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
