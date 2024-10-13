package com.asrevo.cvhome.s2s.config.internal;

import com.asrevo.cvhome.commons.command.*;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandProcessorConfig {
    @Bean
    public CommandProcessor commandProcessor(List<CommandImpl<?>> commandsImpl) {
        //noinspection unchecked
        return new DefaultCommandProcessor(
                commandsImpl.stream().map(it -> ((CommandImpl<Command>) it)).toList());
    }

    @Bean
    public CommandPublisher commandPublisher(ApplicationEventPublisher publisher) {
        return new LocalCommandPublisher(publisher);
    }
}
