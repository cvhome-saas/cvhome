package com.asrevo.cvhome.s2s.config.internal;

import com.asrevo.cvhome.commons.event.*;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;

public class StreamEventProcessorConfig {

    @Bean
    public EventProcessor eventProcessor(List<EventImpl<?>> eventsImpl) {
        //noinspection unchecked
        return new DefaultEventProcessor(
                eventsImpl.stream().map(it -> (EventImpl<Event>) it).toList());
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
