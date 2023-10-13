package com.asrevo.cvhome.store.service;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import com.asrevo.cvhome.store.entity.OwnerEntity;

import java.security.Principal;

public interface OwnerService {
    void addStore(StoreId storeId, IdentityId identityId);

    OwnerEntity getOwnerOrCreate(IdentityId identityId, Principal principal);
}
