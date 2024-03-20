package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.manager.commons.dto.CreateManagerStoreRequest;
import com.asrevo.cvhome.manager.commons.dto.CreateStoreResponse;

public interface StoreManagerService {
    CreateStoreResponse createStore(CreateManagerStoreRequest storeRequest, IdentityId identityId);
}
