package com.asrevo.cvhome.payment.model.payment.event.payment;

import java.util.Map;

import com.asrevo.cvhome.commons.event.Event;

import io.namastack.outbox.annotation.OutboxEvent;

@OutboxEvent(key = "#this.internalRef()")
public record PaymentPaidEvent(String internalRef, String requestRef, String storeId,
                               Map<String, String> data) implements Event {

    public static PaymentPaidEvent from(String internalRef, String requestRef, String storeId) {
        return new PaymentPaidEvent(internalRef, requestRef, storeId, Map.of());
    }

    @Override
    public String eventType() {
        return PaymentPaidEvent.class.getSimpleName();
    }

    @Override
    public Map<String, String> data() {
        return Map.of(
                "internalRef", internalRef,
                "requestRef", requestRef,
                "storeId", storeId
        );
    }
}
