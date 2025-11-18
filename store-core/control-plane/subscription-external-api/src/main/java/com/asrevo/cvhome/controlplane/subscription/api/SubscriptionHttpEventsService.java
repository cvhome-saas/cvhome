package com.asrevo.cvhome.controlplane.subscription.api;

import com.asrevo.cvhome.commons.event.Event;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

@HttpExchange("api/v1/http-events")
public interface SubscriptionHttpEventsService {

	@PostExchange("on")
	Mono<Void> on(@RequestBody Event event);

}
