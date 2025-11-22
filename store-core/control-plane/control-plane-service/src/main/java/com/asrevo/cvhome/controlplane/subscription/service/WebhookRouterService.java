package com.asrevo.cvhome.controlplane.subscription.service;

import com.stripe.model.Event;

public interface WebhookRouterService {

	void route(Event event);

}
