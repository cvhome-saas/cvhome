package com.asrevo.cvhome.tenancy.manager.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.tenancy.commons.dto.CreateStoreRequest;
import com.asrevo.cvhome.tenancy.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.tenancy.commons.dto.StoreStatus;
import com.asrevo.cvhome.tenancy.errors.StoreNotFoundException;
import com.asrevo.cvhome.tenancy.errors.StoreNotOperableException;

public interface InternalStoreService {

    /**
     * Persists the store. A duplicate name surfaces as Spring's {@code DuplicateKeyException} from the unique
     * constraint; it is translated by the caller, outside this method's transaction — see the implementation.
     */
    ManagerStoreDto createStore(CreateStoreRequest request, ManagerOrgId orgId, PodId pod);

    void completeProvisioning(StoreMerchantId store);

    /**
     * Records the store as failed, with the reason the pod gave. The reason is required rather than optional: a
     * FAILED row that says nothing is what left the console able only to report "failed".
     */
    void failProvisioning(StoreMerchantId store, String reason);

    void startProvisioning(StoreMerchantId store);

    Page<ManagerStoreDto> findAll(UserOrgStoreIdentity identity, ListManagerStoreQuery listManagerStoreQuery,
                                  Pageable pageable);

    Page<ManagerStoreDto> findAll(ManagerOrgId id, Pageable pageable);

    /**
     * Unscoped lookup, for callers that have no user — outbox handlers and provisioning. Never reachable from a
     * controller: use the {@link UserOrgStoreIdentity} overload there so a foreign store is refused.
     */
    ManagerStoreDto findStore(StoreMerchantId store) throws StoreNotFoundException;

    /** Refuses, as a 404, a store belonging to an organization other than the caller's. */
    ManagerStoreDto findStore(UserOrgStoreIdentity identity, StoreMerchantId store) throws StoreNotFoundException;

    /** Sets the store's lifecycle status. Transition legality is decided by {@code StoreLifecycleService}. */
    ManagerStoreDto updateStatus(StoreMerchantId store, StoreStatus status) throws StoreNotFoundException;

    /**
     * Refuses a store that is suspended, archived or deleted, or whose organization is not active.
     *
     * @throws StoreNotOperableException the store cannot be worked in right now
     */
    void requireOperable(StoreMerchantId store) throws StoreNotFoundException, StoreNotOperableException;

    Boolean checkNameExists(String name);

    /** Unscoped; see {@link #findStore(StoreMerchantId)}. */
    PodId getStorePod(StoreMerchantId managerStoreId) throws StoreNotFoundException;

    /** Refuses, as a 404, a store belonging to an organization other than the caller's. */
    PodId getStorePod(UserOrgStoreIdentity identity, StoreMerchantId managerStoreId) throws StoreNotFoundException;

}
