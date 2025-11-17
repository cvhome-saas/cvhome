package com.asrevo.cvhome.controlplane.subscription.processors.event;

import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.controlplane.stripe.event.CustomerSubscriptionDeletedEvent;
import com.asrevo.cvhome.controlplane.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerSubscriptionDeletedEventImpl implements EventImpl<CustomerSubscriptionDeletedEvent> {

	private final SubscriptionService subscriptionService;

	@Override
	public void process(CustomerSubscriptionDeletedEvent event) {
		log.info("Received CustomerSubscriptionDeletedEvent from CustomerSubscriptionService {}", event);
		subscriptionService.deActivateSubscription(event.org());
	}

	@Override
	public String type() {
		return CustomerSubscriptionDeletedEvent.class.getSimpleName();
	}

}
