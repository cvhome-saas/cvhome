package com.asrevo.cvhome.s2s.config.internal;

import com.asrevo.cvhome.commons.event.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class EventProcessorConfig {

    @Bean
    public EventProcessor eventProcessor(List<EventImpl<?>> eventsImpl) {
        return new DefaultEventProcessor(eventsImpl.stream().map(it -> (EventImpl<Event>) it).toList());
    }

    @Bean
    public EventPublisher eventPublisher(ApplicationEventPublisher publisher) {
        return new LocalEventPublisher(publisher);
    }
}

