package com.asrevo.cvhome.certificatemanager.config;

import com.asrevo.cvhome.commons.event.*;
import org.springframework.cloud.stream.function.StreamBridge;
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
    public DefaultEventPublisher eventPublisher(StreamBridge streamBridge) {
        return new DefaultEventPublisher(streamBridge);
    }

}

