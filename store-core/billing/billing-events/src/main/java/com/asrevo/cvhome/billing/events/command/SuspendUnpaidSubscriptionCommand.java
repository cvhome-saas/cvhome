package com.asrevo.cvhome.billing.events.command;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;

import io.namastack.outbox.annotation.OutboxEvent;

/**
 * Suspend a store whose grace window after a failed renewal has closed.
 */
@OutboxEvent(key = "#this.store().id().toString()")
public record SuspendUnpaidSubscriptionCommand(ManagerStoreId store, Map<String, String> data) implements SubscriptionCommand {

    public static SuspendUnpaidSubscriptionCommand from(ManagerStoreId store) {
        return new SuspendUnpaidSubscriptionCommand(store, Map.of());
    }

    @Override
    public String eventType() {
        return SuspendUnpaidSubscriptionCommand.class.getSimpleName();
    }

}
