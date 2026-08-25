package com.asrevo.cvhome.billing.events;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import io.namastack.outbox.annotation.OutboxEvent;

/**
 * A store's first paid period opened — the transition from unpaid or trialling to paying.
 */
@OutboxEvent(key = "#this.store().storeMerchantId()")
public record SubscriptionActivatedEvent(StoreMerchantId store, ManagerOrgId org, Map<String, String> data)
        implements SubscriptionEvent {

    public static SubscriptionActivatedEvent from(StoreMerchantId store, ManagerOrgId org) {
        return new SubscriptionActivatedEvent(store, org, Map.of());
    }

    public static SubscriptionActivatedEvent from(StoreMerchantId store, ManagerOrgId org, Map<String, String> data) {
        return new SubscriptionActivatedEvent(store, org, data);
    }

    @Override
    public String eventType() {
        return SubscriptionActivatedEvent.class.getSimpleName();
    }

}
