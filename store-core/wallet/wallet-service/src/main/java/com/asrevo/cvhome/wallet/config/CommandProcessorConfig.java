package com.asrevo.cvhome.wallet.config;

import com.asrevo.cvhome.commons.command.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CommandProcessorConfig {
    @Bean
    public CommandProcessor commandProcessor(List<CommandImpl<?>> commandsImpl) {
        return new DefaultCommandProcessor(commandsImpl.stream().map(it -> ((CommandImpl<Command>) it)).toList());
    }

    @Bean
    public CommandPublisher commandPublisher(ApplicationEventPublisher publisher) {
        return new LocalCommandPublisher(publisher);
    }
}

