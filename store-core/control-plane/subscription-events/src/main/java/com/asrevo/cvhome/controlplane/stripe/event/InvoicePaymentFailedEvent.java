package com.asrevo.cvhome.controlplane.stripe.event;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;

import io.namastack.outbox.annotation.OutboxEvent;

@OutboxEvent(key = "#this.org().id().toString()")
public record InvoicePaymentFailedEvent(ManagerOrgId org, Map<String, String> data) implements StripeEvent {

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
