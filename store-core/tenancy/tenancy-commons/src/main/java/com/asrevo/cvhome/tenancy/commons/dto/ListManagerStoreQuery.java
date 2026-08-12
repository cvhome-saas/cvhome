package com.asrevo.cvhome.tenancy.commons.dto;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public record ListManagerStoreQuery(StoreMerchantId id, String name, IdentityId owner) {
}
