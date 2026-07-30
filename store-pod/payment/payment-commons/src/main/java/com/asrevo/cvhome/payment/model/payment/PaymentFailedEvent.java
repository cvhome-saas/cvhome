package com.asrevo.cvhome.payment.model.payment;

import java.util.Map;

import com.asrevo.cvhome.commons.event.Event;

import io.namastack.outbox.annotation.OutboxEvent;

@OutboxEvent(key = "#this.internalRef()")
public record PaymentFailedEvent(String internalRef, String requestRef, String storeId, String externalId,
                                 Map<String, String> data) implements Event {

    public static PaymentFailedEvent from(String internalRef, String requestRef, String storeId, String externalId) {
        return new PaymentFailedEvent(internalRef, requestRef, storeId, externalId, Map.of());
    }

    @Override
    public String eventType() {
        return PaymentFailedEvent.class.getSimpleName();
    }

    @Override
    public Map<String, String> data() {
        return Map.of(
                "internalRef", internalRef,
                "requestRef", requestRef,
                "storeId", storeId,
                "externalId", externalId
        );
    }
}
