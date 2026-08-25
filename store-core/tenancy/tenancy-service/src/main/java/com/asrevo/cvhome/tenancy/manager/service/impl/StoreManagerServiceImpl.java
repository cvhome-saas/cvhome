package com.asrevo.cvhome.tenancy.manager.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.billing.api.errors.BillingApiUnavailableException;
import com.asrevo.cvhome.billing.api.errors.StoreQuotaRefusedException;
import com.asrevo.cvhome.billing.commons.dto.StoreQuotaDecision;
import com.asrevo.cvhome.billing.commons.dto.StoreQuotaRequest;
import com.asrevo.cvhome.billing.services.quota.ExternalStoreQuotaService;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.merchant.api.MerchantStorePodClient;
import com.asrevo.cvhome.podregistry.api.errors.PodPlacementRefusedException;
import com.asrevo.cvhome.podregistry.api.errors.PodRegistryUnavailableException;
import com.asrevo.cvhome.podregistry.commons.dto.PlacementDecision;
import com.asrevo.cvhome.podregistry.commons.dto.PlacementRequest;
import com.asrevo.cvhome.podregistry.services.placement.ExternalPodPlacementService;
import com.asrevo.cvhome.tenancy.commons.dto.CreateStoreRequest;
import com.asrevo.cvhome.tenancy.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.tenancy.errors.DuplicateStoreNameException;
import com.asrevo.cvhome.tenancy.errors.StoreNotFoundException;
import com.asrevo.cvhome.tenancy.errors.StoreNotOperableException;
import com.asrevo.cvhome.tenancy.manager.mappers.ManagerStoreMappers;
import com.asrevo.cvhome.tenancy.manager.service.InternalStoreService;
import com.asrevo.cvhome.tenancy.manager.service.StoreManagerService;
import com.asrevo.cvhome.tenancy.manager.service.StorePodClientFactory;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StoreManagerServiceImpl implements StoreManagerService {

    /** The pod reference this decorates each store detail with, for the console's store switcher. */
    private static final String POD_KEY = "pod";

    private static final String ID_KEY = "id";

    private final InternalStoreService internalStoreService;

    private final ManagerStoreMappers managerStoreMappers;

    private final StorePodClientFactory podClientFactory;

    private final ExternalPodPlacementService placementService;

    private final ExternalStoreQuotaService billingQuotaService;

    public StoreManagerServiceImpl(InternalStoreService internalStoreService, ManagerStoreMappers managerStoreMappers,
                                   StorePodClientFactory podClientFactory,
                                   ExternalPodPlacementService placementService,
                                   ExternalStoreQuotaService billingQuotaService) {
        this.internalStoreService = internalStoreService;
        this.managerStoreMappers = managerStoreMappers;
        this.podClientFactory = podClientFactory;
        this.placementService = placementService;
        this.billingQuotaService = billingQuotaService;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The billing check runs before a pod is chosen, so a refusal costs nothing. It <em>fails closed</em>: if billing
     * cannot be reached the store is not created. That is the opposite of how the enforcement layers behave — they
     * fail open, because an outage must not take working stores offline — and the asymmetry is deliberate. Refusing
     * to create one store is recoverable by retrying; a store that exists with nobody billed for it is not noticed
     * until someone reconciles revenue.
     * </p>
     *
     * <p>
     * The subscription itself is not created here. {@code StoreCreatedEvent} carries that to
     * {@code BillingProvisioningEventImpl} through the outbox, so provisioning inherits the outbox's retries instead
     * of leaving a window where the store exists and its subscription does not.
     * </p>
     */
    @Override
    public ManagerStoreDto createStore(ManagerOrgId orgId, CreateStoreRequest request)
            throws StoreQuotaRefusedException, BillingApiUnavailableException, PodPlacementRefusedException,
            PodRegistryUnavailableException, DuplicateStoreNameException {
        StoreQuotaDecision decision = billingQuotaService.checkStoreCreate(new StoreQuotaRequest(orgId));
        if (!decision.allowed()) {
            throw StoreQuotaRefusedException.refused(orgId, decision.reason());
        }
        // Five lines of unchecked map-digging until the request became a type.
        String preferred = request.preferredPodId();
        PlacementDecision placement = placementService
                .place(new PlacementRequest(orgId, preferred == null ? null : new PodId(preferred)));
        try {
            return internalStoreService.createStore(request, orgId, placement.podId());
        } catch (DataIntegrityViolationException e) {
            // Caught here, outside createStore's transaction, and not inside it: Postgres aborts a transaction the
            // moment a constraint fails, so catching within would only trade this for an UnexpectedRollbackException
            // at commit — still a 500, minus the cause.
            log.warn("Store name {} collided on the unique constraint", request.getName(), e);
            throw DuplicateStoreNameException.of(request.getName());
        }
    }

    @Override
    public PageImpl<Object> findAll(UserOrgStoreIdentity identity, ListManagerStoreQuery listManagerStoreQuery,
                                    Pageable pageable) {
        Page<ManagerStoreDto> internalStores = internalStoreService.findAll(identity, listManagerStoreQuery, pageable);
        List<Object> list = internalStores.getContent().stream().map(this::withPodDetail).toList();
        return managerStoreMappers.toPage(list, internalStores);
    }

    /**
     * Decorates one row with the detail its pod holds, degrading to the row itself when the pod cannot answer.
     *
     * <p>
     * This used to be {@code catch (Exception e) { return null; }} followed by a filter, so a pod that was slow or
     * down did not degrade the console's main screen — it made stores <em>disappear from it</em>, silently and with
     * nothing logged. A merchant looking at a list with a store missing concludes it was deleted.
     * </p>
     *
     * <p>
     * The catch is deliberately the base type: every failure to reach a pod has the same answer here, which is to
     * show what tenancy knows. The rows are already scoped to the caller's org before this runs, so falling back to
     * the plain row discloses nothing extra.
     * </p>
     */
    private Object withPodDetail(ManagerStoreDto store) {
        try {
            return getStore(store.id());
        } catch (Exception e) {
            log.warn("Could not read store {} from its pod; listing it without pod detail", store.id(), e);
            return store;
        }
    }

    @Override
    public Object getStore(UserOrgStoreIdentity identity, StoreMerchantId managerStoreId)
            throws StoreNotFoundException, StoreNotOperableException {
        internalStoreService.requireOperable(managerStoreId);
        return getStore(internalStoreService.getStorePod(identity, managerStoreId), managerStoreId);
    }

    @Override
    public Object getStore(StoreMerchantId managerStoreId) throws StoreNotFoundException {
        return getStore(internalStoreService.getStorePod(managerStoreId), managerStoreId);
    }

    private Object getStore(PodId podId, StoreMerchantId managerStoreId) {
        MerchantStorePodClient client = podClientFactory.getMerchantStorePodClient(podId);
        Map<String, Object> response = client.getStore(managerStoreId.getId().toString());
        HashMap<String, Object> newIt = new HashMap<>(response);
        newIt.put(POD_KEY, Map.of(ID_KEY, podId.id()));
        return newIt;
    }

}
