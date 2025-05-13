package com.asrevo.cvhome.subscription.processors.event;

import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.stripe.event.InvoicePaymentFailedEvent;
import com.asrevo.cvhome.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoicePaymentFailedEventImpl implements EventImpl<InvoicePaymentFailedEvent> {
    private final SubscriptionService subscriptionService;

    @Override
    public void process(InvoicePaymentFailedEvent event) {
        log.info("Invoice payment failed event received: {}", event);
        subscriptionService.deActivateSubscription(event.org());
    }

    @Override
    public String type() {
        return InvoicePaymentFailedEvent.class.getSimpleName();
    }
}
