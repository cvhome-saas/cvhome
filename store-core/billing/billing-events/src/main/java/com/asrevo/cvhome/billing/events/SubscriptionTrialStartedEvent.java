package com.asrevo.cvhome.billing.events;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import io.namastack.outbox.annotation.OutboxEvent;

/**
 * A store began the org's one trial. Carries the trial end so a notification can say when it runs out.
 */
@OutboxEvent(key = "#this.store().storeMerchantId()")
public record SubscriptionTrialStartedEvent(StoreMerchantId store, ManagerOrgId org, Map<String, String> data)
        implements SubscriptionEvent {

    public static SubscriptionTrialStartedEvent from(StoreMerchantId store, ManagerOrgId org) {
        return new SubscriptionTrialStartedEvent(store, org, Map.of());
    }

    public static SubscriptionTrialStartedEvent from(StoreMerchantId store, ManagerOrgId org, Map<String, String> data) {
        return new SubscriptionTrialStartedEvent(store, org, data);
    }

    @Override
    public String eventType() {
        return SubscriptionTrialStartedEvent.class.getSimpleName();
    }

}
