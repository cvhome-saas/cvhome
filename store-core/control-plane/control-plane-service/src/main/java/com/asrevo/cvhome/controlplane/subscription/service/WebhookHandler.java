package com.asrevo.cvhome.controlplane.subscription.service;

import com.stripe.model.Event;

public interface WebhookHandler {

	void handle(Event event);

	String type();

}
