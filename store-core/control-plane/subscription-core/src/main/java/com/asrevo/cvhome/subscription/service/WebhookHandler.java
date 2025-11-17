package com.asrevo.cvhome.subscription.service;

import com.stripe.model.Event;

public interface WebhookHandler {

	void handle(Event event);

	String type();

}
