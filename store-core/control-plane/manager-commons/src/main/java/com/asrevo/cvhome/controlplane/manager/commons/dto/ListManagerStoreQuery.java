package com.asrevo.cvhome.controlplane.manager.commons.dto;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;

public record ListManagerStoreQuery(ManagerStoreId id, String name, IdentityId owner) {
}
