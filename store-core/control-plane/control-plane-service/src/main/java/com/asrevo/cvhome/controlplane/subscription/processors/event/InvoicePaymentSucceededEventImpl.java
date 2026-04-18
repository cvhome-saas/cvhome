package com.asrevo.cvhome.controlplane.subscription.processors.event;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.controlplane.stripe.event.InvoicePaymentSucceededEvent;
import com.asrevo.cvhome.controlplane.subscription.commons.SubscriptionPlanOption;
import com.asrevo.cvhome.controlplane.subscription.service.SubscriptionPlanTablesService;
import com.asrevo.cvhome.controlplane.subscription.service.SubscriptionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoicePaymentSucceededEventImpl implements EventImpl<InvoicePaymentSucceededEvent> {

    private final SubscriptionService subscriptionService;

    private final SubscriptionPlanTablesService subscriptionPlanTablesService;

    @Override
    public void process(InvoicePaymentSucceededEvent event) {
        log.info("Invoice payment succeeded event: {}", event);
        SubscriptionPlanOption option = subscriptionPlanTablesService.getSubscriptionPlanOption(event.priceId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid subscription price id: " + event.priceId()));
        subscriptionService.renew(event.org(), option.subscriptionPlan(), event.startDate(), event.endDate(),
                option.recurringPlan());
    }

    @Override
    public String type() {
        return InvoicePaymentSucceededEvent.class.getSimpleName();
    }

}
