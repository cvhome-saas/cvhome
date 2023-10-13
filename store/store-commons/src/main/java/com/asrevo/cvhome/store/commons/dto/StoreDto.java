package com.asrevo.cvhome.store.commons.dto;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.store.commons.domain.StoreId;

public record StoreDto(StoreId id, String name, IdentityId owner) {
}
