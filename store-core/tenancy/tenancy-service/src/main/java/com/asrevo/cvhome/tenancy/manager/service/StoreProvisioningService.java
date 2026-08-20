package com.asrevo.cvhome.tenancy.manager.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.FieldError;
import com.asrevo.cvhome.errors.RemoteServiceTimeoutException;
import com.asrevo.cvhome.errors.RemoteServiceUnavailableException;
import com.asrevo.cvhome.errors.UnmappedRemoteFailureException;
import com.asrevo.cvhome.tenancy.commons.dto.CreateStoreRequest;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.tenancy.commons.dto.ProvisioningState;
import com.asrevo.cvhome.tenancy.errors.StoreNotFoundException;

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

    private final StorePodClientFactory podClientFactory;

    private final InternalStoreService internalStoreService;

    /**
     * @throws StoreNotFoundException            the store row is gone — nothing to provision, and retrying will
     *                                           not bring it back
     * @throws RemoteServiceUnavailableException the pod never answered; the caller must let this propagate so the
     *                                           outbox retries
     * @throws RemoteServiceTimeoutException     likewise — the create may or may not have landed
     */
    public void provisioning(ManagerOrgId managerOrgId, StoreMerchantId store, PodId pod, CreateStoreRequest payload)
            throws StoreNotFoundException, RemoteServiceUnavailableException, RemoteServiceTimeoutException {
        if (alreadyProvisioned(store)) {
            // The pod create is not idempotent on the pod's side, so a retry after a create that actually
            // succeeded would create the store twice. Checking our own recorded state first is what stops that.
            log.info("Store {} is already provisioned in pod {}; skipping", store, pod);
            return;
        }
        Map<Object, Object> newRequest = payload.toPodPayload(store.getId().toString(),
                managerOrgId.getId().toString());
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
            String reason = reasonFrom(err);
            internalStoreService.failProvisioning(store, reason);
            log.error("Pod {} refused to create store {}; marked FAILED_PROVISIONING: {}", pod, store, reason, err);
        }
    }

    /**
     * The pod's refusal, reduced to one line worth storing.
     *
     * <p>
     * The pod speaks the same problem-detail contract we do, so a rejected create arrives carrying a code, a detail
     * and — for a validation failure, which is the common case — the exact fields it objected to. All of that used
     * to end at {@code log.error} while the store row recorded nothing but {@code FAILED_PROVISIONING}, and a
     * merchant looking at the console had no way to learn that the problem was a missing phone number.
     * </p>
     *
     * <p>
     * Field names are joined into the line rather than modelled: this is a diagnostic string for a human, not a
     * second binding channel. The synchronous {@code @Valid} on the create endpoint is what gives a form its field
     * errors; anything reaching here is a gap between our validation and the pod's, which someone has to read.
     * </p>
     */
    private static String reasonFrom(UnmappedRemoteFailureException err) {
        ErrorPayload payload = err.payload();
        StringBuilder reason = new StringBuilder(err.remoteCode() == null ? payload.errorCode().code()
                : err.remoteCode());
        if (payload.detail() != null && !payload.detail().isBlank()) {
            reason.append(": ").append(payload.detail());
        }
        List<FieldError> fields = payload.fieldErrors();
        if (!fields.isEmpty()) {
            reason.append(" (")
                    .append(fields.stream().map(StoreProvisioningService::describe).collect(Collectors.joining(", ")))
                    .append(')');
        }
        return reason.toString();
    }

    private static String describe(FieldError field) {
        return field.message() == null || field.message().isBlank() ? field.field()
                : field.field() + " " + field.message();
    }

    private boolean alreadyProvisioned(StoreMerchantId store) throws StoreNotFoundException {
        ManagerStoreDto current = internalStoreService.findStore(store);
        return current.provisioningState() == ProvisioningState.SUCCESSFULLY_PROVISIONING;
    }

}
