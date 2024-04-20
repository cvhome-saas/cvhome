package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.manager.commons.dto.CreateManagerStoreRequest;
import com.asrevo.cvhome.manager.commons.dto.CreateManagerStoreResponse;
import com.asrevo.cvhome.manager.commons.dto.ListManagerStoreQuery;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface StoreManagerService {
    CreateManagerStoreResponse createStore(CreateManagerStoreRequest storeRequest, IdentityId identityId);

    Mono<PageImpl<Object>> findAll(UserOrgStoreIdentity identity, ListManagerStoreQuery listManagerStoreQuery, Pageable pageable);

    Mono<Object> getStore(UserOrgStoreIdentity identity,ManagerStoreId managerStoreId);
}
