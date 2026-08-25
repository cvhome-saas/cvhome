package com.asrevo.cvhome.billing.events.command;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import io.namastack.outbox.annotation.OutboxEvent;

/**
 * Suspend a store whose grace window after a failed renewal has closed.
 */
@OutboxEvent(key = "#this.store().storeMerchantId()")
public record SuspendUnpaidSubscriptionCommand(StoreMerchantId store, Map<String, String> data) implements SubscriptionCommand {

    public static SuspendUnpaidSubscriptionCommand from(StoreMerchantId store) {
        return new SuspendUnpaidSubscriptionCommand(store, Map.of());
    }

    @Override
    public String eventType() {
        return SuspendUnpaidSubscriptionCommand.class.getSimpleName();
    }

}
