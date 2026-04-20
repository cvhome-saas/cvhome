package com.asrevo.cvhome.controlplane.stripe.event;

import java.util.List;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.event.Event;

public sealed interface StripeEvent extends Event
        permits CustomerSubscriptionDeletedEvent, InvoicePaymentFailedEvent, InvoicePaymentSucceededEvent {

    ManagerOrgId org();

    @Override
    default List<String> getDestinations() {
        return List.of("events-out-0");
    }

}
