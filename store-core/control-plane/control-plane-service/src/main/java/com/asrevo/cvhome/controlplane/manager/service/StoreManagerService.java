package com.asrevo.cvhome.controlplane.manager.service;

import java.util.Map;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.controlplane.manager.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.controlplane.manager.commons.dto.ManagerStoreDto;

public interface StoreManagerService {

    ManagerStoreDto createStore(ManagerOrgId orgId, Map<Object, Object> request);

    PageImpl<Object> findAll(UserOrgStoreIdentity identity, ListManagerStoreQuery listManagerStoreQuery,
                             Pageable pageable);

    Object getStore(ManagerStoreId managerStoreId);

}
