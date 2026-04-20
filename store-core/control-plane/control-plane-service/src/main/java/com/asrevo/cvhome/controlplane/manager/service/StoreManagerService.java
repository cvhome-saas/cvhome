package com.asrevo.cvhome.controlplane.manager.service;

import java.util.Map;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.controlplane.manager.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.controlplane.manager.commons.dto.ManagerStoreDto;

import reactor.core.publisher.Mono;

public interface StoreManagerService {

    ManagerStoreDto createStore(ManagerOrgId orgId, Map<Object, Object> request);

    Mono<PageImpl<Object>> findAll(UserOrgStoreIdentity identity, ListManagerStoreQuery listManagerStoreQuery,
                                   Pageable pageable);

    Mono<Object> getStore(ManagerStoreId managerStoreId);

}
