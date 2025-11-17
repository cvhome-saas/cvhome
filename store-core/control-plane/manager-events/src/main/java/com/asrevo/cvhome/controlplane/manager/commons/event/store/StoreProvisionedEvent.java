package com.asrevo.cvhome.controlplane.manager.commons.event.store;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.controlplane.manager.commons.dto.ProvisioningState;
import java.util.Map;

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
