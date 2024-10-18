package com.asrevo.cvhome.s2s.config.internal;

import com.asrevo.cvhome.commons.event.*;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventProcessorConfig {

    @Bean
    public EventProcessor eventProcessor(List<EventImpl<?>> eventsImpl) {
        //noinspection unchecked
        return new DefaultEventProcessor(
                eventsImpl.stream().map(it -> (EventImpl<Event>) it).toList());
    }

    @Bean
    public EventPublisher eventPublisher(ApplicationEventPublisher publisher) {
        return new LocalEventPublisher(publisher);
    }
}
