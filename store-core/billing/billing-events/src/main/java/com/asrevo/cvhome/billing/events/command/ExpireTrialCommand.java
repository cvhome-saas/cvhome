package com.asrevo.cvhome.billing.events.command;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import io.namastack.outbox.annotation.OutboxEvent;

/**
 * Suspend a store whose trial has run out without a payment.
 */
@OutboxEvent(key = "#this.store().storeMerchantId()")
public record ExpireTrialCommand(StoreMerchantId store, Map<String, String> data) implements SubscriptionCommand {

    public static ExpireTrialCommand from(StoreMerchantId store) {
        return new ExpireTrialCommand(store, Map.of());
    }

    @Override
    public String eventType() {
        return ExpireTrialCommand.class.getSimpleName();
    }

}
