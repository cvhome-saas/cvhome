package com.asrevo.cvhome.tenancy.manager.service;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.errors.RemoteServiceUnavailableException;
import com.asrevo.cvhome.errors.UnmappedRemoteFailureException;
import com.asrevo.cvhome.merchant.api.MerchantStorePodClient;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.tenancy.commons.dto.ProvisioningState;
import com.asrevo.cvhome.tenancy.errors.StoreNotFoundException;
import com.asrevo.cvhome.tenancy.manager.mappers.ManagerStoreMappers;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
@Tag("unit-test")
class StoreProvisioningServiceTest {

    private static final ManagerStoreId STORE = new ManagerStoreId("65f023632bc46470c104b76f");

    private static final ManagerOrgId ORG = new ManagerOrgId("21f023932bc66470c104b76f");

    private static final PodId POD = new PodId("507f1f77bcf86cd799439011");

    private static final String MERCHANT = "merchant";

    private InternalStoreService storeService;

    private MerchantStorePodClient podClient;

    private StoreProvisioningService service;

    private static ManagerStoreDto storeIn(ProvisioningState state) {
        return new ManagerStoreDto(STORE, "a-store", ORG, POD, state, null);
    }

    @BeforeEach
    void setUp() throws Exception {
        storeService = mock(InternalStoreService.class);
        podClient = mock(MerchantStorePodClient.class);
        ManagerStoreMappers mappers = mock(ManagerStoreMappers.class);
        when(mappers.toExternalCreateRequest(any(), any(), any())).thenReturn(Map.of());
        StorePodClientFactory factory = mock(StorePodClientFactory.class);
        when(factory.getMerchantStorePodClient(POD)).thenReturn(podClient);
        service = new StoreProvisioningService(mappers, factory, storeService);
    }

    @Test
    @DisplayName("a replayed event does not create the store a second time")
    void replayDoesNotDuplicate() throws Exception {
        when(storeService.findStore(STORE)).thenReturn(storeIn(ProvisioningState.SUCCESSFULLY_PROVISIONING));

        service.provisioning(ORG, STORE, POD, Map.of());

        // The pod's create is not idempotent, so the guard has to be here.
        verify(podClient, never()).create(any());
        verify(storeService, never()).startProvisioning(any());
    }

    @Test
    @DisplayName("a first run provisions and completes")
    void firstRunProvisions() throws Exception {
        when(storeService.findStore(STORE)).thenReturn(storeIn(ProvisioningState.NOT_STARTED_PROVISIONING));

        service.provisioning(ORG, STORE, POD, Map.of());

        verify(podClient).create(any());
        verify(storeService).completeProvisioning(STORE);
    }

    @Test
    @DisplayName("a pod that never answered is left in progress and rethrown, not marked failed")
    void unreachablePodIsNotAFailure() throws Exception {
        when(storeService.findStore(STORE)).thenReturn(storeIn(ProvisioningState.NOT_STARTED_PROVISIONING));
        when(podClient.create(any()))
                .thenThrow(RemoteServiceUnavailableException.of(MERCHANT, Map.of(), new IllegalStateException()));

        assertThatThrownBy(() -> service.provisioning(ORG, STORE, POD, Map.of()))
                .isInstanceOf(RemoteServiceUnavailableException.class);

        // Marking it FAILED would record a verdict nobody reached; the outbox retry is what resolves it.
        verify(storeService, never()).failProvisioning(any());
    }

    @Test
    @DisplayName("a pod that refused is marked failed and swallowed, so the outbox stops retrying")
    void refusalIsTerminal() throws Exception {
        when(storeService.findStore(STORE)).thenReturn(storeIn(ProvisioningState.NOT_STARTED_PROVISIONING));
        when(podClient.create(any())).thenThrow(
                UnmappedRemoteFailureException.of(com.asrevo.cvhome.errors.CommonErrors.REMOTE_UNAVAILABLE,
                        "refused", Map.of(), java.util.List.of(), MERCHANT, "MERCHANT.STORE.INVALID", 422));

        assertThatCode(() -> service.provisioning(ORG, STORE, POD, Map.of())).doesNotThrowAnyException();

        verify(storeService).failProvisioning(STORE);
    }

    @Test
    @DisplayName("a store that no longer exists is abandoned rather than retried forever")
    void missingStoreIsNotFound() throws Exception {
        when(storeService.findStore(STORE)).thenThrow(StoreNotFoundException.of(STORE));

        assertThatThrownBy(() -> service.provisioning(ORG, STORE, POD, Map.of()))
                .isInstanceOf(StoreNotFoundException.class);
    }

}
