package com.asrevo.cvhome.controlplane.subscription.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class PricingTableInitializer implements ApplicationListener<ApplicationReadyEvent> {

	private final PricingTableInitService pricingTableInitService;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		pricingTableInitService.init();
	}

}
