package com.asrevo.cvhome.subscription.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class PricingTableInitializer implements ApplicationListener<ContextStartedEvent> {
    private final PricingTableInitService pricingTableInitService;

    @Override
    public void onApplicationEvent(ContextStartedEvent event) {
        pricingTableInitService.init();
    }
}
