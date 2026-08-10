package com.asrevo.cvhome.controlplane.manager.service;

import java.util.Map;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.billing.api.errors.BillingApiUnavailableException;
import com.asrevo.cvhome.billing.api.errors.StoreQuotaRefusedException;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.controlplane.manager.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.controlplane.manager.commons.dto.ManagerStoreDto;

public interface StoreManagerService {

    /**
     * Creates a store, after billing agrees the org may have another one.
     *
     * @throws StoreQuotaRefusedException     billing refused; the org has to settle what it already holds first
     * @throws BillingApiUnavailableException billing could not be reached. Store creation fails closed on this: a
     *                                       store nobody is billed for is worse than an error the caller can retry
     */
    ManagerStoreDto createStore(ManagerOrgId orgId, Map<Object, Object> request)
            throws StoreQuotaRefusedException, BillingApiUnavailableException;

    PageImpl<Object> findAll(UserOrgStoreIdentity identity, ListManagerStoreQuery listManagerStoreQuery,
                             Pageable pageable);

    Object getStore(ManagerStoreId managerStoreId);

}
