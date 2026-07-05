package com.asrevo.cvhome.controlplane.subscription.processors.event;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.controlplane.stripe.event.CustomerSubscriptionDeletedEvent;
import com.asrevo.cvhome.controlplane.subscription.service.SubscriptionService;

import io.namastack.outbox.annotation.OutboxHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerSubscriptionDeletedEventImpl {

    private final SubscriptionService subscriptionService;

    @OutboxHandler
    public void process(CustomerSubscriptionDeletedEvent event) {
        log.info("Received CustomerSubscriptionDeletedEvent from CustomerSubscriptionService {} from outbox", event);
        subscriptionService.deActivateSubscription(event.org());
    }

}
