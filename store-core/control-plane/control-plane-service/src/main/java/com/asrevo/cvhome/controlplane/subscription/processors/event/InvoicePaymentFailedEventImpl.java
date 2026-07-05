package com.asrevo.cvhome.controlplane.subscription.processors.event;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.controlplane.stripe.event.InvoicePaymentFailedEvent;
import com.asrevo.cvhome.controlplane.subscription.service.SubscriptionService;

import io.namastack.outbox.annotation.OutboxHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoicePaymentFailedEventImpl {

    private final SubscriptionService subscriptionService;

    @OutboxHandler
    public void process(InvoicePaymentFailedEvent event) {
        log.info("Invoice payment failed event received: {} from outbox", event);
        subscriptionService.deActivateSubscription(event.org());
    }

}
