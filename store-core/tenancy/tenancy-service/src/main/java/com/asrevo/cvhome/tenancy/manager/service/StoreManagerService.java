package com.asrevo.cvhome.tenancy.manager.service;

import java.util.Map;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.billing.api.errors.BillingApiUnavailableException;
import com.asrevo.cvhome.billing.api.errors.StoreQuotaRefusedException;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.podregistry.api.errors.PodPlacementRefusedException;
import com.asrevo.cvhome.podregistry.api.errors.PodRegistryUnavailableException;
import com.asrevo.cvhome.tenancy.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.tenancy.errors.DuplicateStoreNameException;
import com.asrevo.cvhome.tenancy.errors.StoreNotFoundException;
import com.asrevo.cvhome.tenancy.errors.StoreNotOperableException;

public interface StoreManagerService {

    /**
     * Creates a store, after billing agrees the org may have another one.
     *
     * @throws StoreQuotaRefusedException     billing refused; the org has to settle what it already holds first
     * @throws BillingApiUnavailableException billing could not be reached. Store creation fails closed on this: a
     *                                       store nobody is billed for is worse than an error the caller can retry
     * @throws PodPlacementRefusedException    the registry has nowhere to put the store. Most often the org's own
     *                                        private pods are all draining, unhealthy or full — and the registry
     *                                        will not substitute a shared pod, so this surfaces rather than
     *                                        silently placing the store on hardware its owner did not agree to
     * @throws PodRegistryUnavailableException the registry could not be reached. Fails closed for the same reason
     *                                        as billing: a store placed on a pod nobody confirmed was eligible is
     *                                        not recoverable by retrying, because the store is already there
     */
    ManagerStoreDto createStore(ManagerOrgId orgId, Map<Object, Object> request)
            throws StoreQuotaRefusedException, BillingApiUnavailableException, PodPlacementRefusedException,
            PodRegistryUnavailableException, DuplicateStoreNameException;

    PageImpl<Object> findAll(UserOrgStoreIdentity identity, ListManagerStoreQuery listManagerStoreQuery,
                             Pageable pageable);

    /**
     * Unscoped: only for rows {@link #findAll} has already confined to the caller's org. A controller must use the
     * {@link UserOrgStoreIdentity} overload, or it will serve another org's store.
     */
    Object getStore(ManagerStoreId managerStoreId) throws StoreNotFoundException;

    /** Refuses, as a 404, a store belonging to an organization other than the caller's. */
    Object getStore(UserOrgStoreIdentity identity, ManagerStoreId managerStoreId)
            throws StoreNotFoundException, StoreNotOperableException;

}
