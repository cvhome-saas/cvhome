package com.asrevo.cvhome.controlplane.manager.commons.dto;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.PodId;

public record ManagerStoreDto(ManagerStoreId id, String name, ManagerOrgId orgId, PodId podId,
		ProvisioningState provisioningState) {
}
