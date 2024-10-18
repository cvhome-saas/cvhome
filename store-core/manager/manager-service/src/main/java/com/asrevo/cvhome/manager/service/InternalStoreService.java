package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.manager.commons.dto.CreateManagerStoreRequest;
import com.asrevo.cvhome.manager.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.manager.commons.dto.ManagerStoreDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InternalStoreService {
    ManagerStoreDto createStore(CreateManagerStoreRequest storeRequest, IdentityId identityId);

    Page<ManagerStoreDto> findAll(
            UserOrgStoreIdentity identity,
            ListManagerStoreQuery listManagerStoreQuery,
            Pageable pageable);

    void syncInRouter(ManagerStoreId store);

    void syncInStore(ManagerStoreId store);

    IdentityId getStoreOwner(ManagerStoreId store);

    ManagerStoreDto findStore(ManagerStoreId store);

    Boolean checkNameExists(String name);
}
