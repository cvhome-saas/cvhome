package com.asrevo.cvhome.store.service;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.store.commons.dto.CreateStoreRequest;
import com.asrevo.cvhome.store.commons.dto.StoreDto;

import java.util.List;

public interface StoreService {
    StoreDto createStore(CreateStoreRequest storeRequest, IdentityId identityId);

    List<StoreDto> findAll(IdentityId identityId);
}
