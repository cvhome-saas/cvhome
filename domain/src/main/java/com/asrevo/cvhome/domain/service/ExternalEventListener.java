package com.asrevo.cvhome.domain.service;

import com.asrevo.cvhome.commons.event.SimpleEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;


@Component
public class ExternalEventListener {
    @Bean
    public Consumer<SimpleEvent> logOrderEvents() {
        return event -> {
            System.out.println("Received: order event: " + event.eventType());
        };
    }
}