package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.commons.domain.*;
import com.asrevo.cvhome.manager.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.manager.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.manager.dto.StoreDomainList;
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

	StoreDomainList domains(ManagerStoreId managerStoreId);

	ManagerStoreId getReferenceByDomain(Domain domain);

	void addDomain(ManagerStoreId managerStoreId, Domain domain);

	void removeDomain(ManagerStoreId managerStoreId, Domain domain);

	Map<String, String> getLookupHeadersByDomain(Domain domain);

}
