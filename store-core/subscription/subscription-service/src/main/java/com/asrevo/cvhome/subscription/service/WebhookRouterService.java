package com.asrevo.cvhome.subscription.service;

import com.stripe.model.Event;

public interface WebhookRouterService {

	void route(Event event);

}
