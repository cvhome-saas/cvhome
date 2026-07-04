package com.asrevo.cvhome.controlplane.subscription.api;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.asrevo.cvhome.commons.event.Event;

@HttpExchange("api/v1/http-events")
public interface SubscriptionHttpEventsService {

    @PostExchange("on")
    void on(@RequestBody Event event);

}
