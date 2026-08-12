package com.asrevo.cvhome.tenancy.manager.service;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.errors.RemoteServiceTimeoutException;
import com.asrevo.cvhome.errors.RemoteServiceUnavailableException;
import com.asrevo.cvhome.errors.UnmappedRemoteFailureException;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.tenancy.commons.dto.ProvisioningState;
import com.asrevo.cvhome.tenancy.errors.StoreNotFoundException;
import com.asrevo.cvhome.tenancy.manager.mappers.ManagerStoreMappers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Creates the store inside its pod, driven from the outbox.
 *
 * <p>
 * Everything here follows from one fact: <strong>this runs again</strong>. It is an outbox handler, so a retry is
 * routine — after a timeout, after a restart mid-flight, after any failure that was not permanent.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StoreProvisioningService {

    private final ManagerStoreMappers managerStoreMappers;

    private final StorePodClientFactory podClientFactory;

    private final InternalStoreService internalStoreService;

    /**
     * @throws StoreNotFoundException            the store row is gone — nothing to provision, and retrying will
     *                                           not bring it back
     * @throws RemoteServiceUnavailableException the pod never answered; the caller must let this propagate so the
     *                                           outbox retries
     * @throws RemoteServiceTimeoutException     likewise — the create may or may not have landed
     */
    public void provisioning(ManagerOrgId managerOrgId, ManagerStoreId store, PodId pod, Map<Object, Object> payload)
            throws StoreNotFoundException, RemoteServiceUnavailableException, RemoteServiceTimeoutException {
        if (alreadyProvisioned(store)) {
            // The pod create is not idempotent on the pod's side, so a retry after a create that actually
            // succeeded would create the store twice. Checking our own recorded state first is what stops that.
            log.info("Store {} is already provisioned in pod {}; skipping", store, pod);
            return;
        }
        Map<Object, Object> newRequest = managerStoreMappers.toExternalCreateRequest(payload, managerOrgId, store);
        internalStoreService.startProvisioning(store);
        try {
            podClientFactory.getMerchantStorePodClient(pod).create(newRequest);
            internalStoreService.completeProvisioning(store);
            log.info("Successfully created new Store {} in Pod {}", store, pod);
        } catch (RemoteServiceUnavailableException | RemoteServiceTimeoutException err) {
            // No answer. The store may or may not exist in the pod, so this is NOT a failure of provisioning —
            // marking it FAILED would record a verdict nobody reached. Left mid-flight and rethrown so the outbox
            // retries; the guard above stops the retry duplicating a create that did land.
            log.warn("Pod {} did not answer while provisioning store {}; leaving it in progress to retry", pod,
                    store, err);
            throw err;
        } catch (UnmappedRemoteFailureException err) {
            // The pod answered, and refused. Retrying an identical request will be refused identically, so this is
            // recorded as failed and swallowed — rethrowing would burn the outbox record's attempts and bury the
            // one line an operator needs to see.
            internalStoreService.failProvisioning(store);
            log.error("Pod {} refused to create store {}; marked FAILED_PROVISIONING", pod, store, err);
        }
    }

    private boolean alreadyProvisioned(ManagerStoreId store) throws StoreNotFoundException {
        ManagerStoreDto current = internalStoreService.findStore(store);
        return current.provisioningState() == ProvisioningState.SUCCESSFULLY_PROVISIONING;
    }

}
