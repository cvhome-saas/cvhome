package com.asrevo.cvhome.controlplane.subscription.processors.event;

import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.controlplane.manager.commons.event.store.OrgCreatedEvent;
import com.asrevo.cvhome.controlplane.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrgCreatedEventImpl implements EventImpl<OrgCreatedEvent> {

	private final SubscriptionService subscriptionService;

	@Override
	public void process(OrgCreatedEvent event) {
		log.info("Received org created event: {}", event);
		subscriptionService.createInitialSubscription(event.org());
	}

	@Override
	public String type() {
		return OrgCreatedEvent.class.getSimpleName();
	}

}
