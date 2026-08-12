package com.asrevo.cvhome.tenancy.events.store;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.tenancy.commons.dto.CreateStoreRequest;

import io.namastack.outbox.annotation.OutboxEvent;

@OutboxEvent(key = "#this.store().storeMerchantId()")
public record StoreCreatedEvent(StoreMerchantId store, ManagerOrgId orgId, PodId podId, Map<String, String> data,
                                CreateStoreRequest request) implements StoreEvent {
    public static StoreCreatedEvent from(StoreMerchantId store, ManagerOrgId orgId, PodId podId,
                                         CreateStoreRequest request) {
        return new StoreCreatedEvent(store, orgId, podId, Map.of(), request);
    }

    @Override
    public String eventType() {
        return StoreCreatedEvent.class.getSimpleName();
    }
}
