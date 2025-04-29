package com.asrevo.cvhome.stripe.event;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import java.util.Map;

public record InvoicePaymentFailedEvent(ManagerOrgId org, Map<String, String> data)
        implements StripeEvent {

    public static InvoicePaymentFailedEvent from(ManagerOrgId org) {
        return new InvoicePaymentFailedEvent(org, Map.of());
    }

    @Override
    public Map<String, String> data() {
        return Map.of();
    }

    public String eventType() {
        return InvoicePaymentFailedEvent.class.getSimpleName();
    }
}
