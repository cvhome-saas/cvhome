package com.asrevo.cvhome.tenancy.manager.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import com.asrevo.cvhome.errors.remote.RemoteErrorContext;
import com.asrevo.cvhome.tenancy.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;
import com.asrevo.cvhome.uaa.api.errors.UaaUserNotFoundException;
import com.asrevo.cvhome.uaa.domain.user.PersistableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUserList;
import com.asrevo.cvhome.uaa.service.UserAccountService;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.ORG_A;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.ORG_B;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.expect;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.json;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.param;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.path;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.query;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.with;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Administering uaa users through tenancy, over real HTTP.
 *
 * <p>
 * uaa keeps {@code org} and {@code store} as free-form user metadata and enforces neither, so every guard that
 * keeps one organization out of another's user records lives on this side of the call — and the permission gate is
 * not it: {@code StoreRoleAccessChecker.isOrgAdmin} ignores the store it is asked about, so the gate admits any org
 * admin to any store. What actually holds is {@code ManagedUserAccountServiceImpl.validateUserAccess}, and that is
 * what the foreign-user cases below exercise: every write is proven to reach uaa on a user that is the caller's and
 * to stop before uaa on one that is not.
 * </p>
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class UserAccountApiIntegrationTest {

    private static final String BASE = "/api/v1/user-account";

    private static final String STORE_PARAM = "store";

    private static final String USER_PARAM = "userId";

    private static final String CURRENT = path(BASE, "current");

    private static final String LIST = "list";

    private static final String FIND_ONE = "find-one";

    private static final String CREATE = "create";

    private static final String UPDATE = "update";

    private static final String RESET = "reset";

    private static final String DELETE = "delete";

    private static final String ENABLE = "enable";

    private static final String DISABLE = "disable";

    private static final String STORE_ADMIN_ROLE = "STORE_ADMIN";

    private static final String STORE_MODERATOR_ROLE = "STORE_MODERATOR";

    private static final String STORE = Tokens.STORE_1;

    private static final String OTHER_STORE = Tokens.STORE_2;

    private static final String USER_ID = "9f1c0e58-0000-4000-8000-000000000001";

    private static final String NEW_PASSWORD = """
            {"password":"old","changePassword":"new-secret"}""";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    @Autowired
    private UserAccountService userAccountService;

    private TenancyApiTestSupport api;

    @BeforeEach
    void setUp() {
        api = new TenancyApiTestSupport(port, signer);
        Mockito.reset(userAccountService);
    }

    private String admin() {
        return api.storeAdmin(ORG_A, STORE);
    }

    private static String scoped(String endpoint, String store) {
        return with(path(BASE, endpoint), STORE_PARAM, store);
    }

    private static String forUser(String endpoint, String store) {
        return query(scoped(endpoint, store), param(USER_PARAM, USER_ID));
    }

    /** A uaa user carrying the metadata that makes it this organization's and this store's. */
    private static ReadableUser userOf(String org, String store) {
        ReadableUser user = new ReadableUser();
        user.setId(USER_ID);
        user.setUserName("someone@example.com");
        user.setOrg(org);
        user.setStore(store);
        return user;
    }

    private void uaaHolds(ReadableUser user) throws Exception {
        when(userAccountService.findOne(USER_ID)).thenReturn(user);
    }

    /** What the uaa SDK throws for a 404 — the shape tenancy restates as its own not-found. */
    private static UaaUserNotFoundException uaaSaysNoSuchUser() {
        return UaaUserNotFoundException.from(new RemoteErrorContext("UAA.USER.NOT_FOUND", "No such user in uaa.",
                Map.of(USER_PARAM, USER_ID), List.of(), "uaa", HttpStatus.NOT_FOUND.value(), null, null));
    }

    /**
     * {@code current} is behind the filter chain like everything else on this controller.
     *
     * <p>
     * Only the authentication half is asserted, because the endpoint cannot answer the other half as written: it
     * takes {@code @AuthenticationPrincipal Principal}, and a resource server's principal is a
     * {@code org.springframework.security.oauth2.jwt.Jwt}, which does not implement {@code Principal}. Spring's
     * resolver hands the method {@code null} rather than failing, so {@code principal.getName()} throws and every
     * authenticated call answers 500. That is a defect in the controller, not in this test, and a test asserting the
     * 500 would only make it permanent — {@code ManagedUserAccountServiceImplTest} covers the lookup itself.
     * </p>
     */
    @Test
    void theCurrentUserEndpointIsAuthenticatedOnly() {
        expect(api.get(CURRENT, null), HttpStatus.UNAUTHORIZED);
    }

    @Test
    void theListIsFilteredByTheCallersOrganizationAndTheRequestedStore() throws Exception {
        ReadableUserList list = new ReadableUserList();
        list.setContent(List.of(userOf(ORG_A, STORE)));
        when(userAccountService.list(any(), anyInt(), anyInt())).thenReturn(list);

        expect(api.get(scoped(LIST, STORE), admin()), HttpStatus.OK);

        ArgumentCaptor<Map<String, String>> filters = ArgumentCaptor.captor();
        verify(userAccountService).list(filters.capture(), anyInt(), anyInt());
        assertThat(filters.getValue()).containsEntry("org", ORG_A).containsEntry(STORE_PARAM, STORE);
    }

    @Test
    void aStoreAdminMayNotListAnotherStoresUsers() {
        expect(api.get(scoped(LIST, OTHER_STORE), admin()), HttpStatus.FORBIDDEN);
    }

    @Test
    void aCustomerMayNotListUsersAtAll() {
        expect(api.get(scoped(LIST, STORE), api.customer(ORG_A, STORE)), HttpStatus.FORBIDDEN);
    }

    @Test
    void anUnauthenticatedCallerIsRefused() {
        expect(api.get(scoped(LIST, STORE), null), HttpStatus.UNAUTHORIZED);
    }

    @Test
    void findOneAnswersAUserOfTheCallersOwnOrganizationAndStore() throws Exception {
        uaaHolds(userOf(ORG_A, STORE));

        JsonNode user = json(api.get(forUser(FIND_ONE, STORE), admin()));

        assertThat(user.get("id").asString()).isEqualTo(USER_ID);
    }

    /**
     * The gate lets an org admin address any store, so the org check has to be in the service — this is the case
     * that proves it is.
     */
    @Test
    void aUserBelongingToAnotherOrganizationIsRefused() throws Exception {
        uaaHolds(userOf(ORG_B, STORE));

        expect(api.get(forUser(FIND_ONE, STORE), api.orgAdmin(ORG_A)), HttpStatus.FORBIDDEN);
    }

    /** A different failure from "not yours", so a caller — and a log — can tell them apart. */
    @Test
    void aUserBelongingToAnotherStoreIsRefused() throws Exception {
        uaaHolds(userOf(ORG_A, OTHER_STORE));

        expect(api.get(forUser(FIND_ONE, STORE), admin()), HttpStatus.FORBIDDEN);
    }

    @Test
    void theAssignableRolesComeFromUaa() throws Exception {
        when(userAccountService.getAssignableRoles()).thenReturn(Set.of(STORE_ADMIN_ROLE, STORE_MODERATOR_ROLE));

        JsonNode roles = json(api.get(path(BASE, "assignable-roles"), admin()));

        assertThat(roles.valueStream().map(JsonNode::asString).toList())
                .containsExactlyInAnyOrder(STORE_ADMIN_ROLE, STORE_MODERATOR_ROLE);
    }

    /** A created user is stamped with the caller's organization and the requested store, never the body's. */
    @Test
    void aCreatedUserIsStampedWithTheCallersTenancy() throws Exception {
        when(userAccountService.createUser(any())).thenReturn(userOf(ORG_A, STORE));

        String bodyClaimingAnotherTenant =
                String.format("{\"emailAddress\":\"new@example.com\",\"org\":\"%s\",\"store\":\"%s\"}",
                        ORG_B, OTHER_STORE);

        expect(api.post(scoped(CREATE, STORE), admin(), bodyClaimingAnotherTenant), HttpStatus.OK);

        ArgumentCaptor<PersistableUser> created = ArgumentCaptor.captor();
        verify(userAccountService).createUser(created.capture());
        assertThat(created.getValue().getOrg()).isEqualTo(ORG_A);
        assertThat(created.getValue().getStore()).isEqualTo(STORE);
        assertThat(created.getValue().isActive()).isTrue();
    }

    @Test
    void aStoreModeratorMayNotCreateAUser() {
        expect(api.post(scoped(CREATE, STORE), api.storeModerator(ORG_A, STORE), "{}"), HttpStatus.FORBIDDEN);
    }

    @Test
    void anUpdateCannotMoveAUserToAnotherOrganization() throws Exception {
        uaaHolds(userOf(ORG_A, STORE));
        when(userAccountService.updateUser(any())).thenReturn(userOf(ORG_A, STORE));

        expect(api.send(HttpMethod.PUT, scoped(UPDATE, STORE), admin(),
                String.format("{\"id\":\"%s\",\"org\":\"%s\",\"store\":\"%s\"}", USER_ID, ORG_B, OTHER_STORE)),
                HttpStatus.OK);

        ArgumentCaptor<PersistableUser> updated = ArgumentCaptor.captor();
        verify(userAccountService).updateUser(updated.capture());
        assertThat(updated.getValue().getOrg()).isEqualTo(ORG_A);
        assertThat(updated.getValue().getStore()).isEqualTo(STORE);
    }

    @Test
    void anUpdateOfAForeignUserNeverReachesUaa() throws Exception {
        uaaHolds(userOf(ORG_B, STORE));

        expect(api.send(HttpMethod.PUT, scoped(UPDATE, STORE), api.orgAdmin(ORG_A),
                String.format("{\"id\":\"%s\"}", USER_ID)), HttpStatus.FORBIDDEN);
        verify(userAccountService, never()).updateUser(any());
    }

    @Test
    void resettingAPasswordReachesUaaWithTheRequestedUser() throws Exception {
        uaaHolds(userOf(ORG_A, STORE));

        expect(api.post(forUser(RESET, STORE), admin(), NEW_PASSWORD), HttpStatus.OK);

        verify(userAccountService).changePassword(eq(USER_ID), any());
    }

    /**
     * Setting another user's password is the maintain audience rather than the read one: a moderator can see who has
     * access to a store without being able to take it over.
     */
    @Test
    void aStoreModeratorMayNotResetAPassword() throws Exception {
        expect(api.post(forUser(RESET, STORE), api.storeModerator(ORG_A, STORE), NEW_PASSWORD),
                HttpStatus.FORBIDDEN);
        verify(userAccountService, never()).changePassword(any(), any());
    }

    @Test
    void resettingAForeignUsersPasswordNeverReachesUaa() throws Exception {
        uaaHolds(userOf(ORG_A, OTHER_STORE));

        expect(api.post(forUser(RESET, STORE), admin(), NEW_PASSWORD), HttpStatus.FORBIDDEN);
        verify(userAccountService, never()).changePassword(any(), any());
    }

    @Test
    void deletingReachesUaaOnlyForTheCallersOwnUser() throws Exception {
        uaaHolds(userOf(ORG_A, STORE));

        expect(api.send(HttpMethod.DELETE, forUser(DELETE, STORE), admin(), null), HttpStatus.OK);

        verify(userAccountService).deleteUser(USER_ID);
    }

    @Test
    void deletingAForeignUserNeverReachesUaa() throws Exception {
        uaaHolds(userOf(ORG_B, STORE));

        expect(api.send(HttpMethod.DELETE, forUser(DELETE, STORE), api.orgAdmin(ORG_A), null),
                HttpStatus.FORBIDDEN);
        verify(userAccountService, never()).deleteUser(any());
    }

    @Test
    void enablingAndDisablingReachUaa() throws Exception {
        uaaHolds(userOf(ORG_A, STORE));

        expect(api.post(forUser(ENABLE, STORE), admin(), null), HttpStatus.OK);
        expect(api.post(forUser(DISABLE, STORE), admin(), null), HttpStatus.OK);

        verify(userAccountService).enableUser(USER_ID);
        verify(userAccountService).disableUser(USER_ID);
    }

    @Test
    void enablingAForeignUserNeverReachesUaa() throws Exception {
        uaaHolds(userOf(ORG_A, OTHER_STORE));

        expect(api.post(forUser(ENABLE, STORE), admin(), null), HttpStatus.FORBIDDEN);
        verify(userAccountService, never()).enableUser(any());
    }

    /**
     * uaa remains the authority for existence, so a user deleted between the guard's read and the write is restated
     * as tenancy's 404 rather than surfacing as whatever the SDK threw.
     */
    @Test
    void aUserThatVanishesBetweenTheGuardAndTheWriteIsAFourOhFour() throws Exception {
        uaaHolds(userOf(ORG_A, STORE));
        Mockito.doThrow(uaaSaysNoSuchUser()).when(userAccountService).disableUser(USER_ID);

        expect(api.post(forUser(DISABLE, STORE), admin(), null), HttpStatus.NOT_FOUND);
    }

}
