package com.asrevo.cvhome.stripe.event;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.event.Event;
import java.util.List;

public sealed interface StripeEvent extends Event
        permits CustomerSubscriptionDeletedEvent,
                InvoicePaymentFailedEvent,
                InvoicePaymentSucceededEvent {
    ManagerOrgId org();

    @Override
    default List<String> getDestinations() {
        return List.of("events-out-0");
    }
}
