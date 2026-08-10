package com.asrevo.cvhome.billing.events;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;

import io.namastack.outbox.annotation.OutboxEvent;

/**
 * A paid period rolled over into the next one.
 */
@OutboxEvent(key = "#this.store().id().toString()")
public record SubscriptionRenewedEvent(ManagerStoreId store, ManagerOrgId org, Map<String, String> data)
        implements SubscriptionEvent {

    public static SubscriptionRenewedEvent from(ManagerStoreId store, ManagerOrgId org) {
        return new SubscriptionRenewedEvent(store, org, Map.of());
    }

    public static SubscriptionRenewedEvent from(ManagerStoreId store, ManagerOrgId org, Map<String, String> data) {
        return new SubscriptionRenewedEvent(store, org, data);
    }

    @Override
    public String eventType() {
        return SubscriptionRenewedEvent.class.getSimpleName();
    }

}
