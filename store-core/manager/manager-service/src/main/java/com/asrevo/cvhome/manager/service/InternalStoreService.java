package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.manager.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.manager.commons.dto.CreateManagerStoreRequest;
import com.asrevo.cvhome.manager.commons.dto.ManagerStoreDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InternalStoreService {
    ManagerStoreDto createStore(CreateManagerStoreRequest storeRequest, IdentityId identityId);

    Page<ManagerStoreDto> findAll(ManagerStoreDto managerStoreDto, IdentityId identityId, Pageable pageable);

    void syncInRouter(ManagerStoreId storeId);

    void syncInStore(ManagerStoreId storeId);
}
