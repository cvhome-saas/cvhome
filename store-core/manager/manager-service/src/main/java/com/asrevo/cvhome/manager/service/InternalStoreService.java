package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.manager.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.manager.commons.dto.CreateManagerStoreRequest;
import com.asrevo.cvhome.manager.commons.dto.ManagerStoreDto;

import java.awt.print.Pageable;
import java.util.List;

public interface InternalStoreService {
    ManagerStoreDto createStore(CreateManagerStoreRequest storeRequest, IdentityId identityId);

    List<ManagerStoreDto> findAll(IdentityId identityId, Pageable pageable);

    void syncInRouter(ManagerStoreId storeId);

    void syncInStore(ManagerStoreId storeId);
}
