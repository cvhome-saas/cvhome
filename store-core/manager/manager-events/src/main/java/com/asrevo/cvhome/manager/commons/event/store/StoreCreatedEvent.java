package com.asrevo.cvhome.manager.commons.event.store;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.event.EventId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;

import java.util.Map;

public record StoreCreatedEvent(EventId eventId, ManagerStoreId storeId, IdentityId identityId,
                                Map<String, String> data) implements StoreEvent {
    public static StoreCreatedEvent from(ManagerStoreId storeId, IdentityId identityId) {
        return new StoreCreatedEvent(EventId.newId(), storeId, identityId, Map.of());
    }

    @Override
    public String eventType() {
        return "StoreCreatedEvent";
    }
}
