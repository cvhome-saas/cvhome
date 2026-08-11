package com.asrevo.cvhome.tenancy.events.store;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.PodId;

import io.namastack.outbox.annotation.OutboxEvent;

@OutboxEvent(key = "#this.store().id().toString()")
public record StoreCreatedEvent(ManagerStoreId store, ManagerOrgId orgId, PodId podId, Map<String, String> data,
                                Map<Object, Object> request) implements StoreEvent {
    public static StoreCreatedEvent from(ManagerStoreId store, ManagerOrgId orgId, PodId podId,
                                         Map<Object, Object> request) {
        return new StoreCreatedEvent(store, orgId, podId, Map.of(), request);
    }

    @Override
    public String eventType() {
        return StoreCreatedEvent.class.getSimpleName();
    }
}
