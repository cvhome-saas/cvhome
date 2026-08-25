package com.asrevo.cvhome.tenancy.manager.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Roles;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;
import com.asrevo.cvhome.tenancy.errors.ForeignOrgUserAccessException;
import com.asrevo.cvhome.tenancy.errors.ForeignStoreUserAccessException;
import com.asrevo.cvhome.tenancy.errors.ManagedUserNotFoundException;
import com.asrevo.cvhome.uaa.api.errors.UaaUserNotFoundException;
import com.asrevo.cvhome.uaa.domain.user.PersistableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUserList;
import com.asrevo.cvhome.uaa.domain.user.UserPassword;
import com.asrevo.cvhome.uaa.service.UserAccountService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The guard that keeps one organization out of another's user records.
 *
 * <p>
 * uaa stores {@code org} and {@code store} as free-form user metadata and enforces neither, so this class is the
 * only thing standing between two tenants. The cases below pin three properties: each refusal has its own type, no
 * write reaches uaa after a refusal, and a user uaa reports as missing becomes tenancy's own not-found rather than
 * whatever the SDK threw.
 * </p>
 */
class ManagedUserAccountServiceImplTest {

    private static final String ORG = "21f023932bc66470c104b76f";

    private static final String OTHER_ORG = "352023632b046970c104b76f";

    private static final String STORE = "65f023632bc46470c104b76f";

    private static final String OTHER_STORE = "65f023632bc46470c104b75f";

    private static final String USER_ID = "9f1c0e58-0000-4000-8000-000000000001";

    private static final UserOrgStoreIdentity IDENTITY =
            new UserOrgStoreIdentity(new ManagerOrgId(ORG), new StoreMerchantId(STORE), Set.of(Roles.ROLE_ORG_ADMIN));

    private static final StoreMerchantId REQUESTED_STORE = new StoreMerchantId(STORE);

    private static final String STORE_ADMIN_ROLE = "STORE_ADMIN";

    private UserAccountService uaa;

    private ManagedUserAccountServiceImpl service;

    @BeforeEach
    void setUp() {
        uaa = mock(UserAccountService.class);
        service = new ManagedUserAccountServiceImpl(uaa);
    }

    private static ReadableUser user(String org, String store) {
        ReadableUser user = new ReadableUser();
        user.setId(USER_ID);
        user.setOrg(org);
        user.setStore(store);
        return user;
    }

    private static UaaUserNotFoundException noSuchUser() {
        return UaaUserNotFoundException.from(new RemoteErrorContext("UAA.USER.NOT_FOUND", "No such user in uaa.",
                Map.of(), List.of(), "uaa", 404, null, null));
    }

    private void uaaHolds(ReadableUser user) throws Exception {
        when(uaa.findOne(USER_ID)).thenReturn(user);
    }

    @Test
    void aUserOfTheSameOrganizationAndStoreIsReturned() throws Exception {
        uaaHolds(user(ORG, STORE));

        assertThat(service.findOne(IDENTITY, REQUESTED_STORE, USER_ID).getId()).isEqualTo(USER_ID);
    }

    /**
     * The lookup used to null-check uaa's answer, which could never fire: the SDK throws on a 404 long before a null
     * can be returned, so a missing user reached the client as a 500 and this service's own 404 was unreachable.
     */
    @Test
    void uaaReportingNoSuchUserBecomesTenancysOwnNotFound() throws Exception {
        when(uaa.findOne(USER_ID)).thenThrow(noSuchUser());

        assertThatThrownBy(() -> service.findOne(USER_ID)).isInstanceOf(ManagedUserNotFoundException.class)
                .hasCauseInstanceOf(UaaUserNotFoundException.class);
    }

    @Test
    void theListIsFilteredByTheCallersOrganizationAndTheRequestedStore() throws Exception {
        when(uaa.list(any(), anyInt(), anyInt())).thenReturn(new ReadableUserList());

        service.list(IDENTITY, REQUESTED_STORE, PageRequest.of(2, 25));

        ArgumentCaptor<Map<String, String>> filters = ArgumentCaptor.captor();
        verify(uaa).list(filters.capture(), anyInt(), anyInt());
        assertThat(filters.getValue()).containsExactlyInAnyOrderEntriesOf(Map.of("org", ORG, "store", STORE));
    }

    @Test
    void theAssignableRolesArePassedThroughUnchanged() throws Exception {
        when(uaa.getAssignableRoles()).thenReturn(Set.of(STORE_ADMIN_ROLE));

        assertThat(service.getAssignableRoles()).containsExactly(STORE_ADMIN_ROLE);
    }

    /** The body cannot choose its own tenancy: both are overwritten from the caller's identity. */
    @Test
    void aCreatedUserIsStampedWithTheCallersTenancyAndActivated() throws Exception {
        PersistableUser body = new PersistableUser();
        body.setOrg(OTHER_ORG);
        body.setStore(OTHER_STORE);

        service.createUser(IDENTITY, REQUESTED_STORE, body);

        assertThat(body.getOrg()).isEqualTo(ORG);
        assertThat(body.getStore()).isEqualTo(STORE);
        assertThat(body.isActive()).isTrue();
        verify(uaa).createUser(body);
    }

    @Test
    void anUpdateCannotMoveAUserToAnotherOrganization() throws Exception {
        uaaHolds(user(ORG, STORE));
        PersistableUser body = new PersistableUser();
        body.setId(USER_ID);
        body.setOrg(OTHER_ORG);
        body.setStore(OTHER_STORE);

        service.updateUser(IDENTITY, REQUESTED_STORE, body);

        assertThat(body.getOrg()).isEqualTo(ORG);
        assertThat(body.getStore()).isEqualTo(STORE);
        verify(uaa).updateUser(body);
    }

    /**
     * uaa remains the authority for existence, so a user deleted between the guard's read and the write is restated
     * as this service's not-found rather than surfacing as the SDK's type.
     */
    @Test
    void aUserThatVanishesBetweenTheGuardAndTheWriteIsRestatedAsNotFound() throws Exception {
        uaaHolds(user(ORG, STORE));
        when(uaa.updateUser(any())).thenThrow(noSuchUser());
        PersistableUser body = new PersistableUser();
        body.setId(USER_ID);

        assertThatThrownBy(() -> service.updateUser(IDENTITY, REQUESTED_STORE, body))
                .isInstanceOf(ManagedUserNotFoundException.class);
    }

    @Test
    void everyWriteReachesUaaForTheCallersOwnUser() throws Exception {
        uaaHolds(user(ORG, STORE));
        UserPassword password = new UserPassword("old", "new");

        service.resetPassword(IDENTITY, REQUESTED_STORE, USER_ID, password);
        service.deleteUser(IDENTITY, REQUESTED_STORE, USER_ID);
        service.enableUser(IDENTITY, REQUESTED_STORE, USER_ID);
        service.disableUser(IDENTITY, REQUESTED_STORE, USER_ID);

        verify(uaa).changePassword(USER_ID, password);
        verify(uaa).deleteUser(USER_ID);
        verify(uaa).enableUser(USER_ID);
        verify(uaa).disableUser(USER_ID);
    }

    /**
     * The two refusals have separate types so a caller — and a log — can tell "not yours" from "wrong store on the
     * request", which the identical {@code ResponseStatusException}s they replaced could not.
     */
    @Nested
    class ARefusalStopsBeforeUaa {

        @Test
        void aUserOfAnotherOrganizationIsAForeignOrgFailure() throws Exception {
            uaaHolds(user(OTHER_ORG, STORE));

            assertThatThrownBy(() -> service.findOne(IDENTITY, REQUESTED_STORE, USER_ID))
                    .isInstanceOf(ForeignOrgUserAccessException.class);
        }

        @Test
        void aUserOfAnotherStoreIsAForeignStoreFailure() throws Exception {
            uaaHolds(user(ORG, OTHER_STORE));

            assertThatThrownBy(() -> service.findOne(IDENTITY, REQUESTED_STORE, USER_ID))
                    .isInstanceOf(ForeignStoreUserAccessException.class);
        }

        @Test
        void noWriteReachesUaaForAForeignUser() throws Exception {
            uaaHolds(user(OTHER_ORG, STORE));

            assertThatThrownBy(() -> service.deleteUser(IDENTITY, REQUESTED_STORE, USER_ID))
                    .isInstanceOf(ForeignOrgUserAccessException.class);
            assertThatThrownBy(() -> service.enableUser(IDENTITY, REQUESTED_STORE, USER_ID))
                    .isInstanceOf(ForeignOrgUserAccessException.class);
            assertThatThrownBy(() -> service.disableUser(IDENTITY, REQUESTED_STORE, USER_ID))
                    .isInstanceOf(ForeignOrgUserAccessException.class);
            assertThatThrownBy(
                    () -> service.resetPassword(IDENTITY, REQUESTED_STORE, USER_ID, new UserPassword("a", "b")))
                    .isInstanceOf(ForeignOrgUserAccessException.class);

            verify(uaa, never()).deleteUser(any());
            verify(uaa, never()).enableUser(any());
            verify(uaa, never()).disableUser(any());
            verify(uaa, never()).changePassword(any(), any());
        }

        @Test
        void anUpdateOfAForeignUserNeverReachesUaa() throws Exception {
            uaaHolds(user(ORG, OTHER_STORE));
            PersistableUser body = new PersistableUser();
            body.setId(USER_ID);

            assertThatThrownBy(() -> service.updateUser(IDENTITY, REQUESTED_STORE, body))
                    .isInstanceOf(ForeignStoreUserAccessException.class);
            verify(uaa, never()).updateUser(any());
        }

    }

}
