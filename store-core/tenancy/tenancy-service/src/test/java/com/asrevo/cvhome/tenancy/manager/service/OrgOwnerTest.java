package com.asrevo.cvhome.tenancy.manager.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerOrgDto;
import com.asrevo.cvhome.tenancy.commons.dto.OrgStatus;
import com.asrevo.cvhome.tenancy.errors.OrgNotFoundException;
import com.asrevo.cvhome.tenancy.errors.OrgOwnerUnknownException;
import com.asrevo.cvhome.tenancy.manager.controller.admin.OrgManagerApi;
import com.asrevo.cvhome.tenancy.manager.dto.CreateOrgRequest;
import com.asrevo.cvhome.tenancy.manager.dto.SignUpUser;
import com.asrevo.cvhome.tenancy.manager.entity.ManagerOrgEntity;
import com.asrevo.cvhome.tenancy.manager.mappers.ManagerOrgMappers;
import com.asrevo.cvhome.tenancy.manager.repository.ManagerOrgRepository;
import com.asrevo.cvhome.tenancy.manager.service.impl.InternalOrgServiceImpl;
import com.asrevo.cvhome.tenancy.manager.service.impl.SignupServiceImpl;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUserList;
import com.asrevo.cvhome.uaa.domain.user.UserPassword;
import com.asrevo.cvhome.uaa.service.UserAccountService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The organization owner nobody recorded.
 *
 * <p>
 * {@code manager_org.owner_user_id} and {@code ManagerOrgDto.ownerUserId} shipped with the lifecycle work and had
 * no writer, so the column was null for every organization on the platform — and
 * {@code OrgManagerApi.changePassword} therefore passed the <em>organization's</em> ObjectId where uaa wanted a
 * user UUID. These pin the three halves of the fix: signup writes it, the backfill resolves the history, and a
 * reset with no owner to act on refuses in a way a caller can read.
 * </p>
 */
class OrgOwnerTest {

    private static final ManagerOrgId ORG = new ManagerOrgId("65f023632bc46470c104b76f");

    private static final String OWNER_ID = "c0ffee00-dead-4bee-8000-000000000001";

    private static final String OWNER_EMAIL = "owner@example.com";

    /** The role {@code SignupServiceImpl} gives an organization's first administrator. */
    private static final String OWNER_ROLE = "ORG_ADMIN";

    private static final String NEW_PASSWORD = "Passw0rd";

    private static final String ORG_NAME = "Nordwerk";

    /** Long enough, unguessable enough and unrelated to the founder's name — the signup rules in one value. */
    private static final String SIGNUP_PASSWORD = "correct-horse-8";

    private ManagerOrgRepository orgRepository;

    private UserAccountService userAccountService;

    private InternalOrgService internalOrgService;

    private static ManagerOrgEntity org(String ownerUserId) {
        ManagerOrgEntity entity = ManagerOrgEntity.createOrgFromUser(new Email(OWNER_EMAIL), ORG_NAME);
        entity.setId(ORG);
        entity.setOwnerUserId(ownerUserId);
        return entity;
    }

    private static ReadableUser user(String id, String username, Set<String> roles) {
        ReadableUser user = new ReadableUser();
        user.setId(id);
        user.setUserName(username);
        user.setRoles(roles);
        return user;
    }

    @BeforeEach
    void setUp() {
        orgRepository = mock(ManagerOrgRepository.class);
        userAccountService = mock(UserAccountService.class);
        when(orgRepository.save(any())).thenAnswer(it -> it.getArgument(0));
        internalOrgService = new InternalOrgServiceImpl(orgRepository, mock(ManagerOrgMappers.class));
    }

    /* --------------------------------------------------------------------------- signup ---- */

    @Test
    @DisplayName("creating an organization records the administrator uaa just made as its owner")
    void signupRecordsTheOwner() throws Exception {
        InternalOrgService orgs = mock(InternalOrgService.class);
        when(orgs.createOrgForUser(any(), any())).thenReturn(ORG);
        when(userAccountService.createUser(any())).thenReturn(user(OWNER_ID, OWNER_EMAIL, Set.of(OWNER_ROLE)));

        new SignupServiceImpl(userAccountService, orgs).createOrgUser(new CreateOrgRequest(
                new SignUpUser("Ada", "Lovelace", OWNER_EMAIL, ORG_NAME, SIGNUP_PASSWORD, SIGNUP_PASSWORD)));

        // The id uaa answered with, not the org's — the confusion that made change-password unimplementable.
        verify(orgs).recordOwner(ORG, OWNER_ID);
    }

    /* ------------------------------------------------------------------------- find-one ---- */

    @Test
    @DisplayName("an unknown org id is a typed 404, not a bare NoSuchElementException")
    void unknownOrgIsNotFound() {
        when(orgRepository.findById(ORG)).thenReturn(Optional.empty());

        // It ended in `.orElseThrow()`, which the error handler can only read as a 500.
        assertThatThrownBy(() -> internalOrgService.findOne(ORG)).isInstanceOf(OrgNotFoundException.class);
        assertThatThrownBy(() -> internalOrgService.recordOwner(ORG, OWNER_ID))
                .isInstanceOf(OrgNotFoundException.class);
    }

    @Test
    @DisplayName("recording an owner writes the column")
    void recordOwnerWritesTheColumn() throws OrgNotFoundException {
        when(orgRepository.findById(ORG)).thenReturn(Optional.of(org(null)));

        internalOrgService.recordOwner(ORG, OWNER_ID);

        ArgumentCaptor<ManagerOrgEntity> saved = ArgumentCaptor.forClass(ManagerOrgEntity.class);
        verify(orgRepository).save(saved.capture());
        assertThat(saved.getValue().getOwnerUserId()).isEqualTo(OWNER_ID);
    }

    /* ------------------------------------------------------------------ change-password ---- */

    @Test
    @DisplayName("a password reset resolves the owner and sends uaa the user id, not the org id")
    void resetResolvesTheOwner() throws Exception {
        InternalOrgService orgs = mock(InternalOrgService.class);
        when(orgs.findOne(ORG)).thenReturn(dto(OWNER_ID));

        api(orgs).changePassword(ORG, new UserPassword(null, NEW_PASSWORD));

        verify(userAccountService).changePassword(eq(OWNER_ID), any());
        // The 24-character ObjectId is what used to be sent, and uaa's @PathVariable UUID could not bind it.
        verify(userAccountService, never()).changePassword(eq(ORG.toString()), any());
    }

    @Test
    @DisplayName("an organization with no recorded owner refuses rather than resetting nobody's password")
    void resetWithoutAnOwnerRefuses() throws Exception {
        InternalOrgService orgs = mock(InternalOrgService.class);
        when(orgs.findOne(ORG)).thenReturn(dto(null));

        assertThatThrownBy(() -> api(orgs).changePassword(ORG, new UserPassword(null, NEW_PASSWORD)))
                .isInstanceOf(OrgOwnerUnknownException.class);

        verify(userAccountService, never()).changePassword(any(), any());
    }

    /**
     * The controller with everything it does not use mocked away.
     *
     * <p>
     * Constructed directly rather than through the Spring context: {@code changePassword} is two statements and a
     * refusal, and standing up a web layer to exercise them would test the framework's binding rather than the
     * decision that was wrong.
     * </p>
     */
    private OrgManagerApi api(InternalOrgService orgs) {
        return new OrgManagerApi(orgs, mock(SignupService.class), userAccountService,
                mock(InternalStoreService.class), mock(OrgLifecycleService.class));
    }

    private static ManagerOrgDto dto(String ownerUserId) {
        return new ManagerOrgDto(ORG, new Email(OWNER_EMAIL), null, ORG_NAME, OrgStatus.ACTIVE,
                ownerUserId);
    }

    /* ------------------------------------------------------------------------ backfill ---- */

    @Test
    @DisplayName("the backfill resolves a historical owner through uaa's org metadata filter")
    void backfillResolvesByMetadata() throws Exception {
        InternalOrgService orgs = mock(InternalOrgService.class);
        when(orgRepository.findWithoutOwner()).thenReturn(List.of(org(null)));
        when(userAccountService.list(eq(Map.of("org", ORG.id().toString())), anyInt(), anyInt()))
                .thenReturn(list(user(OWNER_ID, OWNER_EMAIL, Set.of(OWNER_ROLE))));

        new OrgOwnerBackfill(orgRepository, orgs, userAccountService).backfill();

        verify(orgs).recordOwner(ORG, OWNER_ID);
    }

    @Test
    @DisplayName("with several accounts on one org the ORG_ADMIN wins, and the choice is stable")
    void backfillPrefersTheOrgAdmin() throws Exception {
        InternalOrgService orgs = mock(InternalOrgService.class);
        when(orgRepository.findWithoutOwner()).thenReturn(List.of(org(null)));
        when(userAccountService.list(any(), anyInt(), anyInt()))
                .thenReturn(list(user("aaaa-store", "aaa-store-admin", Set.of("STORE_ADMIN")),
                        user(OWNER_ID, "zzz-owner", Set.of(OWNER_ROLE))));

        new OrgOwnerBackfill(orgRepository, orgs, userAccountService).backfill();

        // Role first, username second — otherwise the store admin sorts ahead on username alone.
        verify(orgs).recordOwner(ORG, OWNER_ID);
    }

    @Test
    @DisplayName("an organization uaa knows nothing about is left unrecorded rather than guessed at")
    void backfillLeavesUnresolvableOrgsAlone() throws Exception {
        InternalOrgService orgs = mock(InternalOrgService.class);
        when(orgRepository.findWithoutOwner()).thenReturn(List.of(org(null)));
        when(userAccountService.list(any(), anyInt(), anyInt())).thenReturn(list());

        new OrgOwnerBackfill(orgRepository, orgs, userAccountService).backfill();

        verify(orgs, never()).recordOwner(any(), any());
    }

    @Test
    @DisplayName("a uaa outage during the backfill costs the owners and nothing else")
    void backfillSurvivesAUaaOutage() throws Exception {
        InternalOrgService orgs = mock(InternalOrgService.class);
        when(orgRepository.findWithoutOwner()).thenReturn(List.of(org(null)));
        when(userAccountService.list(any(), anyInt(), anyInt())).thenThrow(new RuntimeException("uaa is down"));

        // Runs at ApplicationReadyEvent, so throwing here would take a started service down after the fact.
        new OrgOwnerBackfill(orgRepository, orgs, userAccountService).backfill();

        verify(orgs, never()).recordOwner(any(), any());
    }

    private static ReadableUserList list(ReadableUser... users) {
        ReadableUserList result = new ReadableUserList();
        result.setContent(List.of(users));
        result.setTotalElements(users.length);
        return result;
    }

}
