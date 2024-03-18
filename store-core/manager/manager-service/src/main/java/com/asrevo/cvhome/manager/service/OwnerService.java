package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.manager.commons.domain.StoreId;
import com.asrevo.cvhome.manager.entity.OwnerEntity;

import java.security.Principal;

public interface OwnerService {
    void addStore(StoreId storeId, IdentityId identityId);

    OwnerEntity getOwnerOrCreate(IdentityId identityId, Principal principal);
}
