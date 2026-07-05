package com.asrevo.cvhome.controlplane.subscription.processors.event;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.controlplane.stripe.event.InvoicePaymentSucceededEvent;
import com.asrevo.cvhome.controlplane.subscription.commons.SubscriptionPlanOption;
import com.asrevo.cvhome.controlplane.subscription.service.SubscriptionPlanTablesService;
import com.asrevo.cvhome.controlplane.subscription.service.SubscriptionService;

import io.namastack.outbox.annotation.OutboxHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoicePaymentSucceededEventImpl {

    private final SubscriptionService subscriptionService;

    private final SubscriptionPlanTablesService subscriptionPlanTablesService;

    @OutboxHandler
    public void process(InvoicePaymentSucceededEvent event) {
        log.info("Invoice payment succeeded event: {} from outbox", event);
        SubscriptionPlanOption option = subscriptionPlanTablesService.getSubscriptionPlanOption(event.priceId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid subscription price id: " + event.priceId()));
        subscriptionService.renew(event.org(), option.subscriptionPlan(), event.startDate(), event.endDate(),
                option.recurringPlan());
    }

}
