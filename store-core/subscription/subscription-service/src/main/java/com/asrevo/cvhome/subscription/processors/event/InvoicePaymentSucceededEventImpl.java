package com.asrevo.cvhome.subscription.processors.event;

import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.stripe.event.InvoicePaymentSucceededEvent;
import com.asrevo.cvhome.subscription.commons.SubscriptionPlanOption;
import com.asrevo.cvhome.subscription.service.SubscriptionPlanTablesService;
import com.asrevo.cvhome.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoicePaymentSucceededEventImpl implements EventImpl<InvoicePaymentSucceededEvent> {
    private final SubscriptionService subscriptionService;
    private final SubscriptionPlanTablesService subscriptionPlanTablesService;

    @Override
    public void process(InvoicePaymentSucceededEvent event) {
        log.info("Invoice payment succeeded event: {}", event);
        SubscriptionPlanOption option = subscriptionPlanTablesService.getSubscriptionPlanOption(event.priceId()).orElseThrow(() -> new IllegalArgumentException("Invalid subscription price id: " + event.priceId()));
        subscriptionService.renew(event.org(), option.subscriptionPlan(), event.startDate(), event.endDate(), option.recurringPlan());
    }

    @Override
    public String type() {
        return InvoicePaymentSucceededEvent.class.getSimpleName();
    }
}

