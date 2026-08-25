package com.asrevo.cvhome.billing.events.command;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import io.namastack.outbox.annotation.OutboxEvent;

/**
 * Apply a downgrade whose effective date has arrived. The safety net behind the provider's own schedule: the
 * webhook is the primary path, and this covers one that never arrived.
 */
@OutboxEvent(key = "#this.store().storeMerchantId()")
public record ApplyPendingPlanChangeCommand(StoreMerchantId store, Map<String, String> data) implements SubscriptionCommand {

    public static ApplyPendingPlanChangeCommand from(StoreMerchantId store) {
        return new ApplyPendingPlanChangeCommand(store, Map.of());
    }

    @Override
    public String eventType() {
        return ApplyPendingPlanChangeCommand.class.getSimpleName();
    }

}
