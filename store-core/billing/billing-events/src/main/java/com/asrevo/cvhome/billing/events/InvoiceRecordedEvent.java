package com.asrevo.cvhome.billing.events;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import io.namastack.outbox.annotation.OutboxEvent;

/**
 * An invoice was written to billing history.
 */
@OutboxEvent(key = "#this.store().storeMerchantId()")
public record InvoiceRecordedEvent(StoreMerchantId store, ManagerOrgId org, Map<String, String> data)
        implements SubscriptionEvent {

    public static InvoiceRecordedEvent from(StoreMerchantId store, ManagerOrgId org) {
        return new InvoiceRecordedEvent(store, org, Map.of());
    }

    public static InvoiceRecordedEvent from(StoreMerchantId store, ManagerOrgId org, Map<String, String> data) {
        return new InvoiceRecordedEvent(store, org, data);
    }

    @Override
    public String eventType() {
        return InvoiceRecordedEvent.class.getSimpleName();
    }

}
