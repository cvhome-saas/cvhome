package com.asrevo.cvhome.controlplane.manager.service;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.controlplane.manager.mappers.ManagerStoreMappers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StoreProvisioningService {

    private final ManagerStoreMappers managerStoreMappers;

    private final StorePodClientFactory podClientFactory;

    private final InternalStoreService internalStoreService;

    public void provisioning(ManagerOrgId managerOrgId, ManagerStoreId store, PodId pod, Map<Object, Object> payload) {
        Map<Object, Object> newRequest = managerStoreMappers.toExternalCreateRequest(payload, managerOrgId, store);
        internalStoreService.startProvisioning(store);
        podClientFactory.getMerchantStorePodClient(pod).create(newRequest).subscribe((it) -> {
            internalStoreService.completeProvisioning(store);
            log.info("Successfully created new Store {} in Pod {}", store, pod);
        }, (err) -> {
            internalStoreService.failProvisioning(store);
            log.error("Error creating Store in pod {}", pod, err);
        }, () -> {
        });
    }

}
