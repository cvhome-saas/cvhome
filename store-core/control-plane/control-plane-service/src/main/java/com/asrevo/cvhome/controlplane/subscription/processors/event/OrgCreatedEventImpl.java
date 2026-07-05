package com.asrevo.cvhome.controlplane.subscription.processors.event;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.controlplane.manager.commons.event.store.OrgCreatedEvent;
import com.asrevo.cvhome.controlplane.subscription.service.SubscriptionService;

import io.namastack.outbox.annotation.OutboxHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrgCreatedEventImpl {

    private final SubscriptionService subscriptionService;

    @OutboxHandler
    public void process(OrgCreatedEvent event) {
        log.info("Received org created event: {} from outbox", event);
        subscriptionService.createInitialSubscription(event.org());
    }

}
