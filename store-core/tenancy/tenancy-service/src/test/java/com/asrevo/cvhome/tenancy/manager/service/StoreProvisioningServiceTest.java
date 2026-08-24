package com.asrevo.cvhome.tenancy.manager.service;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.errors.FieldError;
import com.asrevo.cvhome.errors.RemoteServiceUnavailableException;
import com.asrevo.cvhome.errors.UnmappedRemoteFailureException;
import com.asrevo.cvhome.merchant.api.MerchantStorePodClient;
import com.asrevo.cvhome.tenancy.commons.dto.CreateStoreRequest;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.tenancy.commons.dto.ProvisioningState;
import com.asrevo.cvhome.tenancy.commons.dto.StoreStatus;
import com.asrevo.cvhome.tenancy.errors.StoreNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Provisioning a store into its pod, which is an outbox handler and therefore runs again.
 *
 * <p>
 * Two things follow from that and are pinned here: a retry after a create that already succeeded must not create
 * the store twice, and a pod that never answered must not be recorded as having refused.
 * </p>
 */
class StoreProvisioningServiceTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final ManagerOrgId ORG = new ManagerOrgId("21f023932bc66470c104b76f");

    private static final PodId POD = new PodId("507f1f77bcf86cd799439011");

    private static final String MERCHANT = "merchant";

    private static final String STORE_NAME = "a-store";

    private static final String REFUSAL_DETAIL = "refused";

    private static final String REFUSAL_CODE = "MERCHANT.STORE.INVALID";

    private static final String FIELD = "email";

    private static final String FIELD_MESSAGE = "must not be null";

    private InternalStoreService storeService;

    private MerchantStorePodClient podClient;

    private StoreProvisioningService service;

    private static CreateStoreRequest request() {
        CreateStoreRequest request = new CreateStoreRequest();
        request.setName(STORE_NAME);
        return request;
    }

    private static ManagerStoreDto storeIn(ProvisioningState state) {
        return new ManagerStoreDto(STORE, STORE_NAME, ORG, POD, state, StoreStatus.ACTIVE, null, null);
    }

    @BeforeEach
    void setUp() throws Exception {
        storeService = mock(InternalStoreService.class);
        podClient = mock(MerchantStorePodClient.class);
        StorePodClientFactory factory = mock(StorePodClientFactory.class);
        when(factory.getMerchantStorePodClient(POD)).thenReturn(podClient);
        service = new StoreProvisioningService(factory, storeService);
    }

    @Test
    @DisplayName("a replayed event does not create the store a second time")
    void replayDoesNotDuplicate() throws Exception {
        when(storeService.findStore(STORE)).thenReturn(storeIn(ProvisioningState.SUCCESSFULLY_PROVISIONING));

        service.provisioning(ORG, STORE, POD, request());

        // The pod's create is not idempotent, so the guard has to be here.
        verify(podClient, never()).create(any());
        verify(storeService, never()).startProvisioning(any());
    }

    @Test
    @DisplayName("a first run provisions and completes")
    void firstRunProvisions() throws Exception {
        when(storeService.findStore(STORE)).thenReturn(storeIn(ProvisioningState.NOT_STARTED_PROVISIONING));

        service.provisioning(ORG, STORE, POD, request());

        verify(podClient).create(any());
        verify(storeService).completeProvisioning(STORE);
    }

    @Test
    @DisplayName("a pod that never answered is left in progress and rethrown, not marked failed")
    void unreachablePodIsNotAFailure() throws Exception {
        when(storeService.findStore(STORE)).thenReturn(storeIn(ProvisioningState.NOT_STARTED_PROVISIONING));
        when(podClient.create(any()))
                .thenThrow(RemoteServiceUnavailableException.of(MERCHANT, Map.of(), new IllegalStateException()));

        assertThatThrownBy(() -> service.provisioning(ORG, STORE, POD, request()))
                .isInstanceOf(RemoteServiceUnavailableException.class);

        // Marking it FAILED would record a verdict nobody reached; the outbox retry is what resolves it.
        verify(storeService, never()).failProvisioning(any(), any());
    }

    @Test
    @DisplayName("a pod that refused is marked failed and swallowed, so the outbox stops retrying")
    void refusalIsTerminal() throws Exception {
        when(storeService.findStore(STORE)).thenReturn(storeIn(ProvisioningState.NOT_STARTED_PROVISIONING));
        when(podClient.create(any())).thenThrow(
                UnmappedRemoteFailureException.of(com.asrevo.cvhome.errors.CommonErrors.REMOTE_UNAVAILABLE,
                        REFUSAL_DETAIL, Map.of(), java.util.List.of(), MERCHANT, REFUSAL_CODE, 422));

        assertThatCode(() -> service.provisioning(ORG, STORE, POD, request())).doesNotThrowAnyException();

        // The reason the pod gave is what the row records; without it the console can only say "failed".
        ArgumentCaptor<String> reason = ArgumentCaptor.forClass(String.class);
        verify(storeService).failProvisioning(eq(STORE), reason.capture());
        assertThat(reason.getValue()).contains(REFUSAL_CODE).contains(REFUSAL_DETAIL);
    }

    @Test
    @DisplayName("the fields the pod objected to are carried into the recorded reason")
    void refusalCarriesFieldErrors() throws Exception {
        when(storeService.findStore(STORE)).thenReturn(storeIn(ProvisioningState.NOT_STARTED_PROVISIONING));
        when(podClient.create(any())).thenThrow(
                UnmappedRemoteFailureException.of(com.asrevo.cvhome.errors.CommonErrors.REMOTE_UNAVAILABLE,
                        "Request validation failed.", Map.of(),
                        java.util.List.of(FieldError.of(FIELD, "VALIDATION.REQUIRED", FIELD_MESSAGE)),
                        MERCHANT, "COMMON.VALIDATION_FAILED", 400));

        service.provisioning(ORG, STORE, POD, request());

        ArgumentCaptor<String> reason = ArgumentCaptor.forClass(String.class);
        verify(storeService).failProvisioning(eq(STORE), reason.capture());
        assertThat(reason.getValue()).contains(FIELD).contains(FIELD_MESSAGE);
    }

    @Test
    @DisplayName("a store that no longer exists is abandoned rather than retried forever")
    void missingStoreIsNotFound() throws Exception {
        when(storeService.findStore(STORE)).thenThrow(StoreNotFoundException.of(STORE));

        assertThatThrownBy(() -> service.provisioning(ORG, STORE, POD, request()))
                .isInstanceOf(StoreNotFoundException.class);
    }

}
