package com.asrevo.cvhome.billing.events;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;

import io.namastack.outbox.annotation.OutboxEvent;

/**
 * A store moved to a different plan. Fired for an applied change, not for a scheduled one — a downgrade that has
 * not taken effect yet changes nothing a consumer should react to.
 */
@OutboxEvent(key = "#this.store().id().toString()")
public record SubscriptionPlanChangedEvent(ManagerStoreId store, ManagerOrgId org, Map<String, String> data)
        implements SubscriptionEvent {

    public static SubscriptionPlanChangedEvent from(ManagerStoreId store, ManagerOrgId org) {
        return new SubscriptionPlanChangedEvent(store, org, Map.of());
    }

    public static SubscriptionPlanChangedEvent from(ManagerStoreId store, ManagerOrgId org, Map<String, String> data) {
        return new SubscriptionPlanChangedEvent(store, org, data);
    }

    @Override
    public String eventType() {
        return SubscriptionPlanChangedEvent.class.getSimpleName();
    }

}
