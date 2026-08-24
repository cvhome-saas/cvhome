package com.asrevo.cvhome.tenancy.manager.service;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.Roles;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.tenancy.commons.dto.ProvisioningState;
import com.asrevo.cvhome.tenancy.commons.dto.StoreStatus;
import com.asrevo.cvhome.tenancy.errors.IllegalLifecycleTransitionException;
import com.asrevo.cvhome.tenancy.errors.StoreNotFoundException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Store lifecycle transitions.
 *
 * <p>
 * The legal moves are a table rather than scattered {@code if}s, and these pin the two properties that matter:
 * DELETED is terminal, and asking for the state a store is already in is tolerated but still audited.
 * </p>
 */
class StoreLifecycleServiceTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final ManagerOrgId ORG = new ManagerOrgId("21f023932bc66470c104b76f");

    private static final PodId POD = new PodId("507f1f77bcf86cd799439011");

    private static final UserOrgStoreIdentity ADMIN =
            new UserOrgStoreIdentity(ORG, new StoreMerchantId("*"), Set.of(Roles.ROLE_ORG_ADMIN));

    private static final String OPERATOR = "super-admin";

    private static final String OWNER = "admin";

    private InternalStoreService storeService;

    private TenancyAuditService auditService;

    private StoreLifecycleService service;

    private static ManagerStoreDto storeIn(StoreStatus status) {
        return new ManagerStoreDto(STORE, "a-store", ORG, POD, ProvisioningState.SUCCESSFULLY_PROVISIONING, status,
                null, null);
    }

    @BeforeEach
    void setUp() throws Exception {
        storeService = mock(InternalStoreService.class);
        auditService = mock(TenancyAuditService.class);
        when(storeService.updateStatus(any(), any()))
                .thenAnswer(it -> storeIn(it.getArgument(1, StoreStatus.class)));
        service = new StoreLifecycleService(storeService, auditService);
    }

    @Test
    @DisplayName("an active store can be suspended, and the change is audited")
    void suspendIsAudited() throws Exception {
        when(storeService.findStore(any(), any())).thenReturn(storeIn(StoreStatus.ACTIVE));

        service.suspend(ADMIN, STORE, OPERATOR, "non-payment");

        verify(storeService).updateStatus(STORE, StoreStatus.SUSPENDED);
        verify(auditService).record(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("a suspended store can be resumed")
    void resumeRestores() throws Exception {
        when(storeService.findStore(any(), any())).thenReturn(storeIn(StoreStatus.SUSPENDED));

        service.resume(ADMIN, STORE, OPERATOR);

        verify(storeService).updateStatus(STORE, StoreStatus.ACTIVE);
    }

    @Test
    @DisplayName("deleting is terminal — nothing brings a deleted store back")
    void deleteIsTerminal() throws Exception {
        when(storeService.findStore(any(), any())).thenReturn(storeIn(StoreStatus.DELETED));

        assertThatThrownBy(() -> service.resume(ADMIN, STORE, OPERATOR))
                .isInstanceOf(IllegalLifecycleTransitionException.class);
        verify(storeService, never()).updateStatus(any(), any());
    }

    @Test
    @DisplayName("suspending an already-suspended store changes nothing but is still recorded")
    void repeatIsANoOpThatAudits() throws Exception {
        when(storeService.findStore(any(), any())).thenReturn(storeIn(StoreStatus.SUSPENDED));

        assertThatCode(() -> service.suspend(ADMIN, STORE, OPERATOR, "again")).doesNotThrowAnyException();

        verify(storeService, never()).updateStatus(any(), any());
        verify(auditService).record(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("an archived store can be reactivated or deleted, but that is all")
    void archivedHasTwoWaysOut() throws Exception {
        when(storeService.findStore(any(), any())).thenReturn(storeIn(StoreStatus.ARCHIVED));

        assertThatCode(() -> service.resume(ADMIN, STORE, OWNER)).doesNotThrowAnyException();
        assertThatThrownBy(() -> service.suspend(ADMIN, STORE, OWNER, "no"))
                .isInstanceOf(IllegalLifecycleTransitionException.class);
    }

    @Test
    @DisplayName("acting on a store the caller cannot see is a 404 from the scoped lookup")
    void foreignStoreIsNotFound() throws Exception {
        when(storeService.findStore(any(), any())).thenThrow(StoreNotFoundException.of(STORE));

        assertThatThrownBy(() -> service.suspend(ADMIN, STORE, OWNER, "x"))
                .isInstanceOf(StoreNotFoundException.class);
    }

}
