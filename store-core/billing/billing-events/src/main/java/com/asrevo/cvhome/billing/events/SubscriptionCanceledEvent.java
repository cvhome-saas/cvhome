package com.asrevo.cvhome.billing.events;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import io.namastack.outbox.annotation.OutboxEvent;

/**
 * A subscription reached its end, whether the customer asked for it or the provider ended it.
 */
@OutboxEvent(key = "#this.store().storeMerchantId()")
public record SubscriptionCanceledEvent(StoreMerchantId store, ManagerOrgId org, Map<String, String> data)
        implements SubscriptionEvent {

    public static SubscriptionCanceledEvent from(StoreMerchantId store, ManagerOrgId org) {
        return new SubscriptionCanceledEvent(store, org, Map.of());
    }

    public static SubscriptionCanceledEvent from(StoreMerchantId store, ManagerOrgId org, Map<String, String> data) {
        return new SubscriptionCanceledEvent(store, org, data);
    }

    @Override
    public String eventType() {
        return SubscriptionCanceledEvent.class.getSimpleName();
    }

}
