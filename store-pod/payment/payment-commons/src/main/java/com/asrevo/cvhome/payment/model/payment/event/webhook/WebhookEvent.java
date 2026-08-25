package com.asrevo.cvhome.payment.model.payment.event.webhook;

import java.util.Map;

import com.asrevo.cvhome.commons.event.Event;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import io.namastack.outbox.annotation.OutboxEvent;

@OutboxEvent(key = "#this.storeId()")
public record WebhookEvent(
        String storeId,
        PaymentType paymentType,
        String payload,
        Map<String, String> headers
) implements Event {

    @Override
    public String eventType() {
        return WebhookEvent.class.getSimpleName();
    }

    @Override
    public Map<String, String> data() {
        return Map.of(
                "storeId", storeId,
                "paymentType", paymentType.name()
        );
    }
}
