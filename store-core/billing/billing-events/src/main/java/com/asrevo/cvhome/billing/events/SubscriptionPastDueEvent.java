package com.asrevo.cvhome.billing.events;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import io.namastack.outbox.annotation.OutboxEvent;

/**
 * A renewal invoice failed. The store keeps working until the grace window closes, which is what makes this worth
 * a notification rather than a silent state change.
 */
@OutboxEvent(key = "#this.store().storeMerchantId()")
public record SubscriptionPastDueEvent(StoreMerchantId store, ManagerOrgId org, Map<String, String> data)
        implements SubscriptionEvent {

    public static SubscriptionPastDueEvent from(StoreMerchantId store, ManagerOrgId org) {
        return new SubscriptionPastDueEvent(store, org, Map.of());
    }

    public static SubscriptionPastDueEvent from(StoreMerchantId store, ManagerOrgId org, Map<String, String> data) {
        return new SubscriptionPastDueEvent(store, org, data);
    }

    @Override
    public String eventType() {
        return SubscriptionPastDueEvent.class.getSimpleName();
    }

}
