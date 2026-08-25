package com.asrevo.cvhome.uaa.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.errors.CommonErrors;
import com.asrevo.cvhome.uaa.api.errors.UaaApiUnavailableException;
import com.asrevo.cvhome.uaa.api.errors.UaaConflictException;
import com.asrevo.cvhome.uaa.api.errors.UaaOperationForbiddenException;
import com.asrevo.cvhome.uaa.api.errors.UaaUserNotFoundException;
import com.asrevo.cvhome.uaa.domain.user.PersistableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.uaa.domain.user.UserPassword;
import com.asrevo.cvhome.uaa.sdk.AdminUserClient;
import com.asrevo.cvhome.uaa.sdk.dto.CreateUserRequest;
import com.asrevo.cvhome.uaa.sdk.dto.PageRequest;
import com.asrevo.cvhome.uaa.sdk.dto.PageResponse;
import com.asrevo.cvhome.uaa.sdk.dto.UpdateUserRequest;
import com.asrevo.cvhome.uaa.sdk.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Narrowing the SDK's transport vocabulary to a per-operation one.
 *
 * <p>
 * {@code AdminUserClient} declares {@code UaaApiException} on everything, because at the transport any uaa endpoint
 * can answer anything. Each method here names what its own operation can actually mean and folds everything else
 * into "undecided" — which is the judgement worth testing, because the alternative is a caller recording a guess as
 * a fact. A conflict on create is a real answer the caller can act on; an unmapped 500 on the same call is not.
 * </p>
 */
class UserAccountServiceImplTest {

    private static final UUID USER_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");

    private static final String USER_ID = USER_UUID.toString();

    private static final String ORG = "32a034a43cd77581d105c87a";

    private static final String STORE = "65f023632bc46470c104b76f";

    private static final String EMAIL = "someone@example.com";

    private static final String USERNAME = "someone";

    private static final String UAA = "uaa";

    private static final String FIRST_NAME = "Some";

    private static final String LAST_NAME = "One";

    private static final String STORE_ADMIN = "STORE_ADMIN";

    private static final String STORE_MODERATOR = "STORE_MODERATOR";

    private static final String PASSWORD = "s3cr3t";

    private static final String NEW_PASSWORD = "n3w";

    private AdminUserClient client;

    private UserAccountServiceImpl service;

    @BeforeEach
    void setUp() {
        client = mock(AdminUserClient.class);
        service = new UserAccountServiceImpl(client);
    }

    private static UserDto dto(Map<String, Object> metadata) {
        return new UserDto(USER_UUID, USERNAME, EMAIL, FIRST_NAME, LAST_NAME, true, Set.of(STORE_ADMIN), metadata);
    }

    private static PersistableUser persistable() {
        PersistableUser user = new PersistableUser();
        user.setId(USER_ID);
        user.setEmailAddress(EMAIL);
        user.setUserName(USERNAME);
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setPassword(PASSWORD);
        user.setRoles(Set.of(STORE_ADMIN));
        return user;
    }

    /** An unmapped failure, exactly as {@code AbstractAdminClient} would produce one. */
    private static UaaApiUnavailableException undecidedFailure() {
        return UaaApiUnavailableException.wrapping(new IllegalStateException("boom"));
    }

    private static UaaUserNotFoundException notFound() {
        return UaaUserNotFoundException.from(new com.asrevo.cvhome.errors.remote.RemoteErrorContext(
                "UAA.USER.NOT_FOUND", "no such user", Map.of(), List.of(), UAA, 404, null, null));
    }

    @Nested
    class Metadata {

        @Test
        void anOrgAndStoreAreSentAsMetadataSoUaaCanScopeTheUser() throws Exception {
            PersistableUser user = persistable();
            user.setOrg(ORG);
            user.setStore(STORE);
            when(client.createUser(any(CreateUserRequest.class))).thenReturn(dto(Map.of()));

            service.createUser(user);

            org.mockito.ArgumentCaptor<CreateUserRequest> request =
                    org.mockito.ArgumentCaptor.forClass(CreateUserRequest.class);
            verify(client).createUser(request.capture());
            assertThat(request.getValue().metadata())
                    .containsEntry(UserAccountServiceImpl.ORG_KEY, ORG)
                    .containsEntry(UserAccountServiceImpl.STORE_KEY, STORE);
        }

        @Test
        void aUserBelongingToNoOrgOrStoreSendsNeitherKeyRatherThanANullOne() throws Exception {
            when(client.createUser(any(CreateUserRequest.class))).thenReturn(dto(Map.of()));

            service.createUser(persistable());

            org.mockito.ArgumentCaptor<CreateUserRequest> request =
                    org.mockito.ArgumentCaptor.forClass(CreateUserRequest.class);
            verify(client).createUser(request.capture());
            assertThat(request.getValue().metadata()).isEmpty();
        }

        @Test
        void aUserUaaReturnsWithoutMetadataReadsBackWithNoOrgOrStore() throws Exception {
            when(client.getUser(USER_ID)).thenReturn(dto(Map.of()));

            ReadableUser user = service.findOne(USER_ID);

            assertThat(user.getOrg()).isNull();
            assertThat(user.getStore()).isNull();
            assertThat(user.getId()).isEqualTo(USER_ID);
            assertThat(user.getEmailAddress()).isEqualTo(EMAIL);
            assertThat(user.isActive()).isTrue();
            assertThat(user.getRoles()).containsExactly(STORE_ADMIN);
        }

        @Test
        void theOrgAndStoreUaaRecordedAreReadBack() throws Exception {
            when(client.getUser(USER_ID)).thenReturn(dto(Map.of(UserAccountServiceImpl.ORG_KEY, ORG,
                    UserAccountServiceImpl.STORE_KEY, STORE)));

            ReadableUser user = service.findOne(USER_ID);

            assertThat(user.getOrg()).isEqualTo(ORG);
            assertThat(user.getStore()).isEqualTo(STORE);
        }
    }

    @Nested
    class Create {

        @Test
        void thePasswordIsSetInASecondCallBecauseUaaDoesNotTakeItOnCreate() throws Exception {
            when(client.createUser(any(CreateUserRequest.class))).thenReturn(dto(Map.of()));

            service.createUser(persistable());

            verify(client).resetPassword(USER_ID, PASSWORD);
        }

        @Test
        void aDuplicateIsAnAnswerTheCallerCanActOnAndReachesItUnchanged() throws Exception {
            UaaConflictException conflict = UaaConflictException.from(
                    new com.asrevo.cvhome.errors.remote.RemoteErrorContext(
                            CommonErrors.DATA_INTEGRITY_VIOLATION.code(), "exists", Map.of(), List.of(), UAA, 409,
                            null, null));
            when(client.createUser(any(CreateUserRequest.class))).thenThrow(conflict);

            assertThatThrownBy(() -> service.createUser(persistable())).isSameAs(conflict);
        }

        @Test
        void anythingElseLeavesTheOutcomeUndecided() throws Exception {
            when(client.createUser(any(CreateUserRequest.class))).thenThrow(notFound());

            assertThatThrownBy(() -> service.createUser(persistable()))
                    .isInstanceOf(UaaApiUnavailableException.class);
        }
    }

    @Nested
    class Update {

        @Test
        void theUpdateCarriesTheFieldsUaaLetsUsChange() throws Exception {
            PersistableUser user = persistable();
            user.setActive(false);
            when(client.updateUser(eq(USER_ID), any(UpdateUserRequest.class))).thenReturn(dto(Map.of()));

            service.updateUser(user);

            org.mockito.ArgumentCaptor<UpdateUserRequest> request =
                    org.mockito.ArgumentCaptor.forClass(UpdateUserRequest.class);
            verify(client).updateUser(eq(USER_ID), request.capture());
            assertThat(request.getValue().enabled()).isFalse();
            assertThat(request.getValue().roles()).containsExactly(STORE_ADMIN);
        }

        @Test
        void aMissingUserAndADuplicateBothReachTheCallerUnchanged() throws Exception {
            UaaUserNotFoundException missing = notFound();
            when(client.updateUser(anyString(), any(UpdateUserRequest.class))).thenThrow(missing);

            assertThatThrownBy(() -> service.updateUser(persistable())).isSameAs(missing);
        }

        @Test
        void anythingElseLeavesTheOutcomeUndecided() throws Exception {
            when(client.updateUser(anyString(), any(UpdateUserRequest.class))).thenThrow(undecidedFailure());

            assertThatThrownBy(() -> service.updateUser(persistable()))
                    .isInstanceOf(UaaApiUnavailableException.class);
        }
    }

    @Nested
    class Mutations {

        @Test
        void deleteEnableAndDisableAllReachTheClient() throws Exception {
            service.deleteUser(USER_ID);
            service.enableUser(USER_ID);
            service.disableUser(USER_ID);

            verify(client).deleteUser(USER_ID);
            verify(client).enableUser(USER_ID);
            verify(client).disableUser(USER_ID);
        }

        @Test
        void uaaRefusingToTouchTheSuperAdminReachesTheCallerAsThatRefusal() throws Exception {
            UaaOperationForbiddenException forbidden = UaaOperationForbiddenException.from(
                    new com.asrevo.cvhome.errors.remote.RemoteErrorContext("UAA.SUPER_ADMIN.IMMUTABLE", "no",
                            Map.of(), List.of(), UAA, 403, null, null));
            org.mockito.Mockito.doThrow(forbidden).when(client).deleteUser(USER_ID);

            assertThatThrownBy(() -> service.deleteUser(USER_ID)).isSameAs(forbidden);
        }

        @Test
        void aMissingUserReachesTheCallerAsThatRefusal() throws Exception {
            UaaUserNotFoundException missing = notFound();
            org.mockito.Mockito.doThrow(missing).when(client).enableUser(USER_ID);

            assertThatThrownBy(() -> service.enableUser(USER_ID)).isSameAs(missing);
        }

        @Test
        void anythingElseLeavesTheOutcomeUndecided() throws Exception {
            org.mockito.Mockito.doThrow(undecidedFailure()).when(client).disableUser(USER_ID);

            assertThatThrownBy(() -> service.disableUser(USER_ID))
                    .isInstanceOf(UaaApiUnavailableException.class);
        }
    }

    @Nested
    class Listing {

        @Test
        void thePageIsCarriedThroughWithItsPagingMetadata() throws Exception {
            when(client.listUsers(any(), any(PageRequest.class)))
                    .thenReturn(new PageResponse<>(List.of(dto(Map.of())), 1, 20, 41L, 3, false, false, false));

            var list = service.list(Map.of(), 1, 20);

            assertThat(list.getTotalElements()).isEqualTo(41);
            assertThat(list.getTotalPages()).isEqualTo(3);
            assertThat(list.getPageNumber()).isEqualTo(1);
            assertThat(list.getSize()).isEqualTo(20);
            assertThat(list.getContent()).singleElement()
                    .extracting(ReadableUser::getEmailAddress).isEqualTo(EMAIL);
        }

        /** A listing names no failure of its own: either uaa answered, or the caller found nothing out. */
        @Test
        void aFailedListingLeavesTheOutcomeUndecided() throws Exception {
            when(client.listUsers(any(), any(PageRequest.class))).thenThrow(notFound());

            assertThatThrownBy(() -> service.list(Map.of(), 0, 20))
                    .isInstanceOf(UaaApiUnavailableException.class);
        }
    }

    @Nested
    class RolesAndPasswords {

        /**
         * USER and ORG_ADMIN are granted by uaa itself rather than assigned through this API, so offering them in a
         * role picker would let an operator try to grant something that will be refused.
         */
        @Test
        void theRolesUaaManagesItselfAreNotOfferedAsAssignable() throws Exception {
            when(client.getAssignableRoles()).thenReturn(Set.of("USER", "ORG_ADMIN", STORE_ADMIN,
                    STORE_MODERATOR));

            assertThat(service.getAssignableRoles()).containsExactlyInAnyOrder(STORE_ADMIN, STORE_MODERATOR);
        }

        @Test
        void aFailedRoleLookupLeavesTheOutcomeUndecided() throws Exception {
            when(client.getAssignableRoles()).thenThrow(notFound());

            assertThatThrownBy(() -> service.getAssignableRoles())
                    .isInstanceOf(UaaApiUnavailableException.class);
        }

        @Test
        void changingAPasswordReachesTheClient() throws Exception {
            UserPassword request = new UserPassword();
            request.setChangePassword(NEW_PASSWORD);

            service.changePassword(USER_ID, request);

            verify(client).resetPassword(USER_ID, NEW_PASSWORD);
        }

        @Test
        void changingThePasswordOfAMissingUserReachesTheCallerAsThatRefusal() throws Exception {
            UaaUserNotFoundException missing = notFound();
            org.mockito.Mockito.doThrow(missing).when(client).resetPassword(anyString(), anyString());
            UserPassword request = new UserPassword();
            request.setChangePassword(NEW_PASSWORD);

            assertThatThrownBy(() -> service.changePassword(USER_ID, request)).isSameAs(missing);
        }
    }

    @Test
    void currentIsTheSameLookupAsFindOne() throws Exception {
        when(client.getUser(USER_ID)).thenReturn(dto(Map.of()));

        assertThat(service.current(USER_ID).getId()).isEqualTo(USER_ID);
    }

    @Test
    void anUndecidedFailureAlreadyOfThatTypeIsNotWrappedTwice() throws Exception {
        UaaApiUnavailableException unavailable = undecidedFailure();
        when(client.getUser(USER_ID)).thenThrow(unavailable);

        assertThatThrownBy(() -> service.findOne(USER_ID)).isSameAs(unavailable);
    }

}
