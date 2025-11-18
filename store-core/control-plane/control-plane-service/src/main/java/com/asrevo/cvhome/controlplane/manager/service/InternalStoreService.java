package com.asrevo.cvhome.controlplane.manager.service;

import com.asrevo.cvhome.commons.domain.*;
import com.asrevo.cvhome.controlplane.manager.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.controlplane.manager.commons.dto.ManagerStoreDto;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InternalStoreService {

	ManagerStoreDto createStore(Map<Object, Object> request, ManagerOrgId orgId, PodId pod);

	void completeProvisioning(ManagerStoreId store);

	void failProvisioning(ManagerStoreId store);

	void startProvisioning(ManagerStoreId store);

	Page<ManagerStoreDto> findAll(UserOrgStoreIdentity identity, ListManagerStoreQuery listManagerStoreQuery,
			Pageable pageable);

	Page<ManagerStoreDto> findAll(ManagerOrgId id, Pageable pageable);

	ManagerOrgId getStoreOwner(ManagerStoreId store);

	ManagerStoreDto findStore(ManagerStoreId store);

	Boolean checkNameExists(String name);

	Pod getStorePod(ManagerStoreId managerStoreId);

}
