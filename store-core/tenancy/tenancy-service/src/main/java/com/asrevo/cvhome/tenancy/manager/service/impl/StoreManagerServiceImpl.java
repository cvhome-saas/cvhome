package com.asrevo.cvhome.tenancy.manager.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.merchant.api.MerchantStorePodClient;
import com.asrevo.cvhome.podregistry.api.errors.PodPlacementRefusedException;
import com.asrevo.cvhome.podregistry.api.errors.PodRegistryUnavailableException;
import com.asrevo.cvhome.podregistry.commons.dto.PlacementDecision;
import com.asrevo.cvhome.podregistry.commons.dto.PlacementRequest;
import com.asrevo.cvhome.podregistry.services.placement.ExternalPodPlacementService;
import com.asrevo.cvhome.tenancy.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.tenancy.errors.StoreNotFoundException;
import com.asrevo.cvhome.tenancy.manager.mappers.ManagerStoreMappers;
import com.asrevo.cvhome.tenancy.manager.service.InternalStoreService;
import com.asrevo.cvhome.tenancy.manager.service.StoreManagerService;
import com.asrevo.cvhome.tenancy.manager.service.StorePodClientFactory;

@Service
public class StoreManagerServiceImpl implements StoreManagerService {

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
    public ManagerStoreDto createStore(ManagerOrgId orgId, Map<Object, Object> request)
            throws StoreQuotaRefusedException, BillingApiUnavailableException, PodPlacementRefusedException,
            PodRegistryUnavailableException {
        StoreQuotaDecision decision = billingQuotaService.checkStoreCreate(new StoreQuotaRequest(orgId));
        if (!decision.allowed()) {
            throw StoreQuotaRefusedException.refused(orgId, decision.reason());
        }
        PodId prefaredPodId = Optional.ofNullable(request.get(POD_KEY))
                .map(it -> (Map<String, String>) it)
                .filter(it -> it.containsKey(ID_KEY))
                .map(it -> it.get(ID_KEY))
                .filter(it -> !it.trim().isEmpty())
                .map(PodId::new)
                .orElse(null);
        PlacementDecision placement = placementService.place(new PlacementRequest(orgId, prefaredPodId));
        return internalStoreService.createStore(request, orgId, placement.podId());
    }

    @Override
    public PageImpl<Object> findAll(UserOrgStoreIdentity identity, ListManagerStoreQuery listManagerStoreQuery,
                                    Pageable pageable) {
        Page<ManagerStoreDto> internalStores = internalStoreService.findAll(identity, listManagerStoreQuery, pageable);
        List<Object> list = internalStores.getContent().stream()
                .map(it -> {
                    try {
                        return getStore(it.id());
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
        return managerStoreMappers.toPage(list, internalStores);
    }

    @Override
    public Object getStore(UserOrgStoreIdentity identity, ManagerStoreId managerStoreId)
            throws StoreNotFoundException {
        return getStore(internalStoreService.getStorePod(identity, managerStoreId), managerStoreId);
    }

    @Override
    public Object getStore(ManagerStoreId managerStoreId) throws StoreNotFoundException {
        return getStore(internalStoreService.getStorePod(managerStoreId), managerStoreId);
    }

    private Object getStore(PodId podId, ManagerStoreId managerStoreId) {
        MerchantStorePodClient client = podClientFactory.getMerchantStorePodClient(podId);
        Map<String, Object> response = client.getStore(managerStoreId.getId().toString());
        HashMap<String, Object> newIt = new HashMap<>(response);
        newIt.put(POD_KEY, Map.of(ID_KEY, podId.id()));
        return newIt;
    }

}
