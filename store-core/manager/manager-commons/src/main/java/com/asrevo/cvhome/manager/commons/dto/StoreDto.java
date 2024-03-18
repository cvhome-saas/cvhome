package com.asrevo.cvhome.manager.commons.dto;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.manager.commons.domain.StoreId;

public record StoreDto(StoreId id, String name, IdentityId owner) {
}
