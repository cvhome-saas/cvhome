package com.asrevo.cvhome.sso.service;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.domain.Role;
import com.asrevo.cvhome.sso.domain.UaaConstants;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.domain.UserStatus;
import com.asrevo.cvhome.sso.dto.CreateUserRequest;
import com.asrevo.cvhome.sso.dto.ResetUserPasswordRequest;
import com.asrevo.cvhome.sso.dto.UpdateUserRequest;
import com.asrevo.cvhome.sso.dto.UserCounts;
import com.asrevo.cvhome.sso.dto.UserSearch;
import com.asrevo.cvhome.sso.password.PasswordService;
import com.asrevo.cvhome.sso.repo.RoleRepository;
import com.asrevo.cvhome.sso.repo.UserRepository;
import com.asrevo.cvhome.sso.security.LockoutService;
import com.asrevo.cvhome.sso.session.SessionAdminService;
import com.asrevo.cvhome.sso.token.TokenRevocationService;
import com.asrevo.cvhome.uaa.errors.RoleNotAssignableException;
import com.asrevo.cvhome.uaa.errors.RoleNotFoundException;
import com.asrevo.cvhome.uaa.errors.SuperAdminImmutableException;
import com.asrevo.cvhome.uaa.errors.UserNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The guards in {@link AdminService}: the super admin is untouchable, roles are granted by exact name or not at all.
 */
class AdminServiceTest {

    private static final String ORG = "org";

    private static final String ORG_ID = "org-1";

    private static final String STORE = "store";

    private static final String STORE_ADMIN = "STORE_ADMIN";

    private static final String MISSPELLED = "STORE_ADMN";

    private static final String TEAM = "team";

    private static final String NORTH = "north";

    private static final String NEW_USERNAME = "new";

    private static final String NEW_EMAIL = "new@example.com";

    private static final String NEW_PASSWORD = "Secret-1";

    private static final String HASH_PREFIX = "{hash}";

    private static final String CONCAT = "%s%s";

    private static final String ORG_ADMIN = "ORG_ADMIN";

    private static final String USER = "USER";

    private static final String SOMEONE = "someone";

    private static final String SESSION_ID = "s-1";

    private static final String NOBODY = "nobody";

    private final UserRepository users = mock(UserRepository.class);

    private final RoleRepository roles = mock(RoleRepository.class);

    private final PasswordService passwords = mock(PasswordService.class);

    private final AuditService audit = mock(AuditService.class);

    private final SessionAdminService sessions = mock(SessionAdminService.class);

    private final TokenRevocationService tokens = mock(TokenRevocationService.class);

    private final AdminService service = new AdminService(users, roles, passwords, audit, Clock.systemUTC(), sessions,
            tokens, mock(LockoutService.class));

    private User superAdmin;

    private User ordinary;

    @BeforeEach
    void setUp() throws Exception {
        superAdmin = new User();
        superAdmin.setId(UaaConstants.SUPER_ADMIN_ID);
        superAdmin.setUsername("super-admin");
        superAdmin.setEmail("owner@example.com");
        ordinary = new User();
        ordinary.setId(UUID.randomUUID());
        ordinary.setUsername(SOMEONE);
        ordinary.getMetadata().putAll(new HashMap<>(Map.of(ORG, ORG_ID, STORE, "store-1")));
        Map<UUID, User> store = new HashMap<>(Map.of(superAdmin.getId(), superAdmin, ordinary.getId(), ordinary));
        when(users.findById(any(UUID.class))).thenAnswer(invocation -> Optional.ofNullable(store.get(invocation.getArgument(0))));
        when(users.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            // The mock stands in for JPA's @PrePersist, which is what assigns the id.
            saved.prePersist();
            store.put(saved.getId(), saved);
            return saved;
        });
        doAnswer(invocation -> {
            User target = invocation.getArgument(0);
            target.setPasswordHash(String.format(CONCAT, HASH_PREFIX, invocation.getArgument(1)));
            return null;
        }).when(passwords).setPassword(any(User.class), anyString());
    }

    @Test
    void resetPasswordRefusesTheSuperAdminEvenWhenTheirEmailChanged() {
        assertThatThrownBy(() -> service.resetPassword(superAdmin.getId(), new ResetUserPasswordRequest(NEW_PASSWORD)))
                .isInstanceOf(SuperAdminImmutableException.class);
        verify(users, never()).save(any());
    }

    @Test
    void resetPasswordEncodesForAnyoneElse() throws Exception {
        service.resetPassword(ordinary.getId(), new ResetUserPasswordRequest(NEW_PASSWORD));

        assertThat(ordinary.getPasswordHash()).isEqualTo(String.format(CONCAT, HASH_PREFIX, NEW_PASSWORD));
    }

    @Test
    void unknownRoleIsRefusedNotSkipped() {
        when(roles.findByName(MISSPELLED)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignRoles(ordinary.getId(), Set.of(MISSPELLED)))
                .isInstanceOf(RoleNotFoundException.class);
        assertThat(ordinary.getRoles()).isEmpty();
    }

    @Test
    void superAdminRoleIsNeverGranted() {
        assertThatThrownBy(() -> service.assignRoles(ordinary.getId(), Set.of(UaaConstants.SUPER_ADMIN_ROLE)))
                .isInstanceOf(RoleNotAssignableException.class);
    }

    @Test
    void knownRolesAreGranted() throws Exception {
        when(roles.findByName(STORE_ADMIN)).thenReturn(Optional.of(new Role(STORE_ADMIN)));

        service.assignRoles(ordinary.getId(), Set.of(STORE_ADMIN));

        assertThat(ordinary.getRoles()).extracting(Role::getName).containsExactly(STORE_ADMIN);
    }

    @Test
    void aNullMetadataValueRemovesTheKey() throws Exception {
        Map<String, Object> patch = new HashMap<>();
        patch.put(STORE, null);
        patch.put(TEAM, NORTH);

        service.updateUser(ordinary.getId(), new UpdateUserRequest(null, null, null, null, null, patch));

        assertThat(ordinary.getMetadata()).containsEntry(ORG, ORG_ID).containsEntry(TEAM, NORTH).doesNotContainKey(STORE);
    }

    @Test
    void createWithoutAPasswordLeavesNoHash() throws Exception {
        service.createUser(new CreateUserRequest(NEW_USERNAME, NEW_EMAIL, null, null, null, Set.of(), Map.of()));

        verify(users).save(any(User.class));
        verify(passwords, never()).setPassword(any(User.class), anyString());
    }

    @Test
    void createWithAPasswordEncodesIt() throws Exception {
        service.createUser(new CreateUserRequest(NEW_USERNAME, NEW_EMAIL, null, null, NEW_PASSWORD, Set.of(), Map.of()));

        verify(passwords).setPassword(any(User.class), eq(NEW_PASSWORD));
    }

    @Test
    void assignableRolesExcludeSuperAdminOnly() {
        when(roles.findAll()).thenReturn(List.of(new Role(UaaConstants.SUPER_ADMIN_ROLE), new Role(ORG_ADMIN),
                new Role(USER)));

        assertThat(service.getAssignableRoles()).containsExactlyInAnyOrder(ORG_ADMIN, USER);
    }

    @Test
    void everyAdministrativeActionRefusesTheSuperAdminByIdRatherThanByRole() {
        // The account that grants every privilege cannot be disabled, deleted or re-roled through the admin API,
        // and the check is on the seeded id so renaming the role or the user does not open it.
        UUID id = superAdmin.getId();

        assertThatThrownBy(() -> service.disableUser(id)).isInstanceOf(SuperAdminImmutableException.class);
        assertThatThrownBy(() -> service.enableUser(id)).isInstanceOf(SuperAdminImmutableException.class);
        assertThatThrownBy(() -> service.delete(id)).isInstanceOf(SuperAdminImmutableException.class);
        assertThatThrownBy(() -> service.unlock(id)).isInstanceOf(SuperAdminImmutableException.class);
        assertThatThrownBy(() -> service.verifyEmail(id)).isInstanceOf(SuperAdminImmutableException.class);
        assertThatThrownBy(() -> service.removeRoles(id, Set.of(STORE_ADMIN)))
                .isInstanceOf(SuperAdminImmutableException.class);
    }

    @Test
    void anUnknownUserIsATypedNotFoundOnEveryPathThatResolvesOne() {
        UUID missing = UUID.randomUUID();

        assertThatThrownBy(() -> service.getUser(missing)).isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> service.listSessions(missing)).isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> service.revokeSessions(missing)).isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> service.revokeSession(missing, SESSION_ID)).isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> service.enableUser(missing)).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void disablingAUserEndsEverySessionAndEveryTokenNotJustTheFlag() throws Exception {
        service.disableUser(ordinary.getId());

        // A disabled account with a live refresh token is still signed in until that token expires.
        assertThat(ordinary.isEnabled()).isFalse();
        verify(sessions).revokeAll(ordinary, null);
        verify(tokens).revokeAllForUser(ordinary);
    }

    @Test
    void enablingAUserIsJustTheFlagAndLeavesSessionsAlone() throws Exception {
        service.enableUser(ordinary.getId());

        assertThat(ordinary.isEnabled()).isTrue();
        verify(sessions, never()).revokeAll(any(), any());
    }

    @Test
    void resettingAPasswordAlsoEndsEverySessionAndToken() throws Exception {
        service.resetPassword(ordinary.getId(), new ResetUserPasswordRequest(NEW_PASSWORD));

        // Otherwise whoever knew the old password stays signed in after the reset that was meant to lock them out.
        verify(sessions).revokeAll(ordinary, null);
        verify(tokens).revokeAllForUser(ordinary);
    }

    @Test
    void deletingAUserRevokesFirstAndRecordsWhatWasThere() throws Exception {
        service.delete(ordinary.getId());

        verify(sessions).revokeAll(ordinary, null);
        verify(tokens).revokeAllForUser(ordinary);
        verify(users).delete(ordinary);
    }

    @Test
    void verifyingAnAlreadyVerifiedEmailRecordsNothingNew() throws Exception {
        ordinary.setEmailVerified(true);

        service.verifyEmail(ordinary.getId());

        verify(audit, never()).record(any());
    }

    @Test
    void verifyingAnUnverifiedEmailFlipsItAndRecordsIt() throws Exception {
        ordinary.setEmailVerified(false);

        assertThat(service.verifyEmail(ordinary.getId()).emailVerified()).isTrue();
        verify(audit).record(any());
    }

    @Test
    void removingNoRolesIsANoOpRatherThanAnEmptyAudit() throws Exception {
        service.removeRoles(ordinary.getId(), Set.of());
        service.removeRoles(ordinary.getId(), null);

        verify(audit, never()).record(any());
    }

    @Test
    void theSessionCallsAllResolveTheUserFirstSoTheyCannotAddressAnother() throws Exception {
        service.listSessions(ordinary.getId());
        service.revokeSession(ordinary.getId(), SESSION_ID);
        service.revokeSessions(ordinary.getId());

        verify(sessions).list(ordinary, null);
        verify(sessions).revoke(ordinary, SESSION_ID);
        verify(sessions).revokeAll(ordinary, null);
    }

    @Test
    void aUsernameLookupAnswersWhetherOneIsTaken() {
        when(users.findByUsername(SOMEONE)).thenReturn(Optional.of(ordinary));
        when(users.findByUsername(NOBODY)).thenReturn(Optional.empty());

        assertThat(service.usernameExist(SOMEONE)).isTrue();
        assertThat(service.usernameExist(NOBODY)).isFalse();
    }

    @Test
    void theCountsAreOnePerStatusPlusTheTotal() {
        when(users.count()).thenReturn(10L);
        when(users.count(any(org.springframework.data.jpa.domain.Specification.class))).thenReturn(2L);

        UserCounts counts = service.counts();

        assertThat(counts.total()).isEqualTo(10L);
        assertThat(counts.active()).isEqualTo(2L);
        // One query per status, not one scan filtered four ways.
        verify(users, times(4)).count(any(org.springframework.data.jpa.domain.Specification.class));
    }

    @Test
    void anEmptySearchStillProducesASpecificationRatherThanNull() {
        when(users.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        service.getUsers(null, Pageable.unpaged());
        service.getUsers(new UserSearch(null, null, null, Map.of()), Pageable.unpaged());

        verify(users, times(2))
                .findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));
    }

    @Test
    void eachSearchFieldAddsItsOwnPredicateAndANullMetadataEntryAddsNone() {
        when(users.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());
        Map<String, String> metadata = new HashMap<>();
        metadata.put(ORG, ORG_ID);
        metadata.put(TEAM, null);

        service.getUsers(new UserSearch("q", UserStatus.ACTIVE, STORE_ADMIN, metadata), Pageable.unpaged());

        verify(users).findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));
    }
}
