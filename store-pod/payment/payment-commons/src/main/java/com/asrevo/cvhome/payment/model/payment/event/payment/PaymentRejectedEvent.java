package com.asrevo.cvhome.payment.model.payment.event.payment;

import java.util.Map;

import com.asrevo.cvhome.commons.event.Event;

import io.namastack.outbox.annotation.OutboxEvent;

/**
 * A store admin rejected a manual-transfer proof. Emitted so checkout learns of it — before this event a rejected
 * transfer left the order pending forever.
 */
@OutboxEvent(key = "#this.internalRef()")
public record PaymentRejectedEvent(String internalRef, String requestRef, String storeId,
                                   Map<String, String> data) implements Event {

    public static PaymentRejectedEvent from(String internalRef, String requestRef, String storeId) {
        return new PaymentRejectedEvent(internalRef, requestRef, storeId, Map.of());
    }

    @Override
    public String eventType() {
        return PaymentRejectedEvent.class.getSimpleName();
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
