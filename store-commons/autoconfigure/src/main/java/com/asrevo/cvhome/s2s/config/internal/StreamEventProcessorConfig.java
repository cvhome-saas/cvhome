package com.asrevo.cvhome.s2s.config.internal;

import java.util.List;
import java.util.function.Consumer;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;

import com.asrevo.cvhome.commons.event.DefaultEventProcessor;
import com.asrevo.cvhome.commons.event.Event;
import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.commons.event.EventProcessor;
import com.asrevo.cvhome.commons.event.EventPublisher;
import com.asrevo.cvhome.commons.event.StreamEventPublisher;

public class StreamEventProcessorConfig {

    @Bean
    public EventProcessor eventProcessor(List<EventImpl<?>> eventsImpl) {
        // noinspection unchecked
        return new DefaultEventProcessor(eventsImpl.stream().map(it -> (EventImpl<Event>) it).toList());
    }

    @Bean
    public Consumer<Event> events(EventProcessor eventProcessor) {
        return eventProcessor::process;
    }

    @Bean
    public EventPublisher eventPublisher(StreamBridge streamBridge) {
        return new StreamEventPublisher(streamBridge);
    }

}
