package com.asrevo.cvhome.billing.events;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import io.namastack.outbox.annotation.OutboxEvent;

/**
 * A store lost access — a trial that ran out, or a grace window that closed.
 */
@OutboxEvent(key = "#this.store().storeMerchantId()")
public record SubscriptionSuspendedEvent(StoreMerchantId store, ManagerOrgId org, Map<String, String> data)
        implements SubscriptionEvent {

    public static SubscriptionSuspendedEvent from(StoreMerchantId store, ManagerOrgId org) {
        return new SubscriptionSuspendedEvent(store, org, Map.of());
    }

    public static SubscriptionSuspendedEvent from(StoreMerchantId store, ManagerOrgId org, Map<String, String> data) {
        return new SubscriptionSuspendedEvent(store, org, data);
    }

    @Override
    public String eventType() {
        return SubscriptionSuspendedEvent.class.getSimpleName();
    }

}
