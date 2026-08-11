package com.asrevo.cvhome.tenancy.manager.service;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.tenancy.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.tenancy.errors.StoreNotFoundException;

public interface InternalStoreService {

    ManagerStoreDto createStore(Map<Object, Object> request, ManagerOrgId orgId, PodId pod);

    void completeProvisioning(ManagerStoreId store);

    void failProvisioning(ManagerStoreId store);

    void startProvisioning(ManagerStoreId store);

    Page<ManagerStoreDto> findAll(UserOrgStoreIdentity identity, ListManagerStoreQuery listManagerStoreQuery,
                                  Pageable pageable);

    Page<ManagerStoreDto> findAll(ManagerOrgId id, Pageable pageable);

    /**
     * Unscoped lookup, for callers that have no user — outbox handlers and provisioning. Never reachable from a
     * controller: use the {@link UserOrgStoreIdentity} overload there so a foreign store is refused.
     */
    ManagerStoreDto findStore(ManagerStoreId store) throws StoreNotFoundException;

    /** Refuses, as a 404, a store belonging to an organization other than the caller's. */
    ManagerStoreDto findStore(UserOrgStoreIdentity identity, ManagerStoreId store) throws StoreNotFoundException;

    Boolean checkNameExists(String name);

    /** Unscoped; see {@link #findStore(ManagerStoreId)}. */
    PodId getStorePod(ManagerStoreId managerStoreId) throws StoreNotFoundException;

    /** Refuses, as a 404, a store belonging to an organization other than the caller's. */
    PodId getStorePod(UserOrgStoreIdentity identity, ManagerStoreId managerStoreId) throws StoreNotFoundException;

}
