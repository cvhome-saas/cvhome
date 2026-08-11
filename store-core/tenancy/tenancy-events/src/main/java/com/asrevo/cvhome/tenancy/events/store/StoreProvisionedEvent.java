package com.asrevo.cvhome.tenancy.events.store;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.tenancy.commons.dto.ProvisioningState;

public record StoreProvisionedEvent(ManagerStoreId store, PodId podId, ProvisioningState provisioningState,
                                    Map<String, String> data) implements StoreEvent {
    public static StoreProvisionedEvent from(ManagerStoreId store, PodId podId, ProvisioningState provisioningState) {
        return new StoreProvisionedEvent(store, podId, provisioningState, Map.of());
    }

    @Override
    public String eventType() {
        return StoreProvisionedEvent.class.getSimpleName();
    }
}
