package com.asrevo.cvhome.manager.commons.dto;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.manager.commons.domain.ManagerStoreId;

public record ListManagerStoreQuery(ManagerStoreId id, String name, IdentityId owner) {
}
