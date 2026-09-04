package com.asrevo.cvhome.tenancy.manager.controller;

import java.security.Principal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Roles;
import com.asrevo.cvhome.commons.domain.StatisticEntry;
import com.asrevo.cvhome.commons.domain.StatisticRange;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerOrgDto;
import com.asrevo.cvhome.tenancy.controller.AuthApi;
import com.asrevo.cvhome.tenancy.errors.OrgOwnerUnknownException;
import com.asrevo.cvhome.tenancy.manager.controller.admin.OrgManagerApi;
import com.asrevo.cvhome.tenancy.manager.controller.statistic.OrgStatisticApi;
import com.asrevo.cvhome.tenancy.manager.controller.statistic.StoreStatisticApi;
import com.asrevo.cvhome.tenancy.manager.entity.TenancyAuditEntity;
import com.asrevo.cvhome.tenancy.manager.repository.ManagerOrgRepository;
import com.asrevo.cvhome.tenancy.manager.repository.ManagerStoreRepository;
import com.asrevo.cvhome.tenancy.manager.repository.TenancyAuditRepository;
import com.asrevo.cvhome.tenancy.manager.service.InternalOrgService;
import com.asrevo.cvhome.tenancy.manager.service.InternalStoreService;
import com.asrevo.cvhome.tenancy.manager.service.ManagedUserAccountService;
import com.asrevo.cvhome.tenancy.manager.service.OrgLifecycleService;
import com.asrevo.cvhome.tenancy.manager.service.SignupService;
import com.asrevo.cvhome.tenancy.manager.service.TenancyAuditService;
import com.asrevo.cvhome.uaa.service.UserAccountService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The platform-operator endpoints, the user-account endpoints, and the audit writer they all go through.
 *
 * <p>
 * Two things here are more than delegation. Changing an organisation's password resolves its owner first and
 * refuses when the org has none, rather than calling uaa with a null user id — the org would otherwise get a 500
 * from a downstream service instead of an answer about its own state. And every audit row records an actor: the API
 * writer takes it from the caller, the job writer hard-codes {@code system}, and the two are separate methods
 * precisely so a scheduled job cannot be recorded as whoever happened to be signed in.
 * </p>
 */
class TenancyAdminApisTest {

    private static final ManagerOrgId ORG = new ManagerOrgId("21f023932bc66470c104b76f");
    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");
    private static final String OPERATOR = "ops@example.com";
    private static final String UNKNOWN_ACTOR = "unknown";
    private static final String USER_ID = "user-1";
    private static final String OWNER_ID = "owner-1";
    private static final String NEW_NAME = "new name";
    private static final String REASON = "non-payment";

    private final InternalOrgService internalOrgService = Mockito.mock(InternalOrgService.class);
    private final SignupService signupService = Mockito.mock(SignupService.class);
    private final UserAccountService userAccountService = Mockito.mock(UserAccountService.class);
    private final InternalStoreService internalStoreService = Mockito.mock(InternalStoreService.class);
    private final OrgLifecycleService orgLifecycleService = Mockito.mock(OrgLifecycleService.class);
    private final ManagedUserAccountService managedUserAccountService =
            Mockito.mock(ManagedUserAccountService.class);
    private final ManagerStoreRepository storeRepository = Mockito.mock(ManagerStoreRepository.class);
    private final ManagerOrgRepository orgRepository = Mockito.mock(ManagerOrgRepository.class);
    private final TenancyAuditRepository auditRepository = Mockito.mock(TenancyAuditRepository.class);

    private final OrgManagerApi orgManagerApi = new OrgManagerApi(internalOrgService, signupService,
            userAccountService, internalStoreService, orgLifecycleService);
    private final UserAccountApi userAccountApi = new UserAccountApi(managedUserAccountService);
    private final AuthApi authApi = new AuthApi();
    private final StoreStatisticApi storeStatisticApi = new StoreStatisticApi(storeRepository);
    private final OrgStatisticApi orgStatisticApi = new OrgStatisticApi(orgRepository);
    private final TenancyAuditService auditService = new TenancyAuditService(auditRepository);

    private static Authentication operator() {
        return new UsernamePasswordAuthenticationToken(OPERATOR, null, List.of());
    }

    private static UserOrgStoreIdentity identity() {
        return new UserOrgStoreIdentity(ORG, STORE, Set.of(Roles.ROLE_ORG_ADMIN));
    }

    @Test
    void theOrgReadEndpointsAllDelegateWithTheirQuery() throws Exception {
        when(internalOrgService.findAll(any(PageRequest.class))).thenReturn(Page.empty());
        when(internalOrgService.findAll(any(), any())).thenReturn(Page.empty());
        when(internalStoreService.findAll(eq(ORG), any())).thenReturn(Page.empty());

        orgManagerApi.findAllOrg(PageRequest.of(0, 20));
        orgManagerApi.listOrgs(null, PageRequest.of(0, 20));
        orgManagerApi.findOne(ORG);
        orgManagerApi.create(null);
        orgManagerApi.findAllStores(ORG, PageRequest.of(0, 20));

        verify(internalOrgService).findAll(any(PageRequest.class));
        verify(internalOrgService).findAll(eq(null), any());
        verify(internalOrgService).findOne(ORG);
        verify(signupService).createOrgUser(null);
        verify(internalStoreService).findAll(eq(ORG), any());
    }

    @Test
    void changingAnOrgsPasswordResolvesItsOwnerFirst() throws Exception {
        when(internalOrgService.findOne(ORG)).thenReturn(orgWithOwner(OWNER_ID));

        orgManagerApi.changePassword(ORG, null);

        verify(userAccountService).changePassword(OWNER_ID, null);
    }

    @Test
    void anOrgWithNoOwnerIsRefusedRatherThanCallingUaaWithANullUserId() throws Exception {
        when(internalOrgService.findOne(ORG)).thenReturn(orgWithOwner(null));

        assertThatThrownBy(() -> orgManagerApi.changePassword(ORG, null))
                .isInstanceOf(OrgOwnerUnknownException.class);
        Mockito.verifyNoInteractions(userAccountService);
    }

    @Test
    void anOrgWithABlankOwnerIsRefusedTheSameWay() throws Exception {
        when(internalOrgService.findOne(ORG)).thenReturn(orgWithOwner("   "));

        assertThatThrownBy(() -> orgManagerApi.changePassword(ORG, null))
                .isInstanceOf(OrgOwnerUnknownException.class);
    }

    @Test
    void everyOrgLifecycleTransitionRecordsAnActorAndDefaultsItsReason() throws Exception {
        orgManagerApi.rename(ORG, NEW_NAME, operator());
        orgManagerApi.suspend(ORG, null, operator());
        orgManagerApi.suspend(ORG, REASON, null);
        orgManagerApi.resume(ORG, operator());
        orgManagerApi.close(ORG, null);

        verify(orgLifecycleService).rename(ORG, NEW_NAME, OPERATOR);
        verify(orgLifecycleService).suspend(ORG, OPERATOR, "suspended by operator");
        verify(orgLifecycleService).suspend(ORG, UNKNOWN_ACTOR, REASON);
        verify(orgLifecycleService).resume(ORG, OPERATOR);
        verify(orgLifecycleService).close(ORG, UNKNOWN_ACTOR);
    }

    @Test
    void everyUserAccountEndpointCarriesTheIdentityAndTheStore() throws Exception {
        Principal principal = () -> USER_ID;

        userAccountApi.current(principal);
        userAccountApi.list(principal, identity(), STORE, PageRequest.of(0, 20));
        userAccountApi.findOne(identity(), STORE, USER_ID);
        userAccountApi.assignableRoles();
        userAccountApi.create(identity(), STORE, null);
        userAccountApi.update(identity(), STORE, null);
        userAccountApi.resetPassword(identity(), STORE, USER_ID, null);
        userAccountApi.delete(identity(), STORE, USER_ID);
        userAccountApi.enable(identity(), STORE, USER_ID);
        userAccountApi.disable(identity(), STORE, USER_ID);

        verify(managedUserAccountService).findOne(USER_ID);
        verify(managedUserAccountService).list(any(), eq(STORE), any());
        verify(managedUserAccountService).findOne(any(), eq(STORE), eq(USER_ID));
        verify(managedUserAccountService).getAssignableRoles();
        verify(managedUserAccountService).createUser(any(), eq(STORE), eq(null));
        verify(managedUserAccountService).updateUser(any(), eq(STORE), eq(null));
        verify(managedUserAccountService).resetPassword(any(), eq(STORE), eq(USER_ID), eq(null));
        verify(managedUserAccountService).deleteUser(any(), eq(STORE), eq(USER_ID));
        verify(managedUserAccountService).enableUser(any(), eq(STORE), eq(USER_ID));
        verify(managedUserAccountService).disableUser(any(), eq(STORE), eq(USER_ID));
    }

    @Test
    void theCurrentPrincipalEndpointAnswersUnauthorizedRatherThanNull() {
        Principal principal = () -> USER_ID;

        assertThat(authApi.current(principal).getBody()).isSameAs(principal);
        assertThat(authApi.current(null).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void bothStatisticEndpointsWrapTheirRowsInAList() {
        ZonedDateTime from = ZonedDateTime.parse("2026-01-01T00:00:00Z");
        StatisticRange range = new StatisticRange(from, from.plusDays(1));
        when(storeRepository.storeStatistic(any(), any())).thenReturn(List.of(StatisticEntry.of("stores", 2)));
        when(orgRepository.orgStatistic(any(), any())).thenReturn(List.of(StatisticEntry.of("orgs", 1)));

        assertThat(storeStatisticApi.storeStatistic(range).entries()).hasSize(1);
        assertThat(orgStatisticApi.orgStatistic(range).entries()).hasSize(1);

        // The range arrives as ZonedDateTime and the repositories take Instant; the conversion is the endpoint's.
        verify(storeRepository).storeStatistic(from.toInstant(), from.plusDays(1).toInstant());
    }

    @Test
    void anApiAuditRowNamesItsCallerAndAJobRowNamesTheSystem() {
        ArgumentCaptor<TenancyAuditEntity> rows = ArgumentCaptor.forClass(TenancyAuditEntity.class);

        auditService.record(null, ORG, "SUSPEND", null, null, OPERATOR, "by hand");
        auditService.recordJob(null, ORG, "BACKFILL", null, null, "nightly");

        verify(auditRepository, Mockito.times(2)).save(rows.capture());
        assertThat(rows.getAllValues()).extracting(TenancyAuditEntity::getActor)
                .containsExactly(OPERATOR, "system");
    }

    /** A record, so it is built rather than mocked -- stubbing one inside another when() breaks the outer stub. */
    private static ManagerOrgDto orgWithOwner(String ownerUserId) {
        return new ManagerOrgDto(ORG, new Email("owner@example.com"), Instant.EPOCH, "an org", null, ownerUserId);
    }
}
