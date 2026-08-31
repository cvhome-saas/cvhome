package com.asrevo.cvhome.tenancy.manager.controller.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.asrevo.cvhome.tenancy.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.uaa.domain.user.PersistableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.uaa.domain.user.UserPassword;
import com.asrevo.cvhome.uaa.service.UserAccountService;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.ORG_A;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.ORG_CLOSED;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.ORG_LIFECYCLE;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.ORG_OWNED;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.ORG_OWNER_USER_ID;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.expect;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.idOf;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.idsOf;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.json;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.param;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.path;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.query;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.with;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.slug;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The platform operator's view of organizations, over real HTTP.
 *
 * <p>
 * Every endpoint on this controller is {@code ROLE_SUPER_ADMIN} only, and that is the security property under test:
 * an organization administrator is a tenant, not an operator, so their token must be refused here even for their
 * <em>own</em> organization — otherwise this controller is a way around the org scoping the rest of the service
 * enforces.
 * </p>
 *
 * <p>
 * The lifecycle cases act on {@code ORG_LIFECYCLE} and {@code ORG_CLOSED} rather than on the seeded organizations
 * the store tests read, because closing an organization is terminal.
 * </p>
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class OrgManagerApiIntegrationTest {

    private static final String BASE = "/api/v1/org-manager";

    private static final String FIND_ALL = path(BASE, "find-all");

    private static final String LIST = path(BASE, "list");

    private static final String FIND_ONE = path(BASE, "find-one");

    private static final String STORES = path(BASE, "stores");

    private static final String RENAME = path(BASE, "rename");

    private static final String SUSPEND = path(BASE, "suspend");

    private static final String RESUME = path(BASE, "resume");

    private static final String CLOSE = path(BASE, "close");

    private static final String CREATE = path(BASE, "create");

    private static final String CHANGE_PASSWORD = path(BASE, "change-password");

    private static final String ID = "id";

    private static final String ACTIVE = "ACTIVE";

    private static final String UNKNOWN_ORG = "0000000000000000000000ff";

    private static final String NEW_PASSWORD = """
            {"password":"old","changePassword":"new-secret"}""";

    private static final String STATUS = "status";

    private static final String NAME = "name";

    private static final String CONTENT = "content";

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

    private ResponseEntity<String> asOperator(String url) {
        return api.get(url, api.superAdmin());
    }

    private static String forOrg(String path, String org) {
        return with(path, ID, org);
    }

    @Test
    void everyOrganizationOnThePlatformIsListed() {
        ResponseEntity<String> response = asOperator(FIND_ALL);

        expect(response, HttpStatus.OK);
        assertThat(idsOf(json(response))).contains(ORG_A, ORG_LIFECYCLE, ORG_CLOSED);
    }

    /**
     * The one endpoint on the controller that had no annotation at all: without it any authenticated principal could
     * enumerate every organization on the platform.
     */
    @Test
    void anOrganizationAdminMayNotListOrganizations() {
        expect(api.get(FIND_ALL, api.orgAdmin(ORG_A)), HttpStatus.FORBIDDEN);
    }

    @Test
    void anOrganizationAdminMayNotReadAnotherOrganization() {
        expect(api.get(forOrg(FIND_ONE, ORG_LIFECYCLE), api.orgAdmin(ORG_A)), HttpStatus.FORBIDDEN);
    }

    @Test
    void anOrganizationAdminMayNotEvenReadTheirOwn() {
        expect(api.get(forOrg(FIND_ONE, ORG_A), api.orgAdmin(ORG_A)), HttpStatus.FORBIDDEN);
    }

    @Test
    void anUnauthenticatedCallerIsRefused() {
        expect(api.get(FIND_ALL, null), HttpStatus.UNAUTHORIZED);
    }

    /**
     * Almost every organization on the platform is unnamed, so the console lists them by email — one predicate has
     * to span both or the search box fails to find exactly the rows on screen.
     */
    @Test
    void theTermSpansTheNameAndTheContactEmail() {
        JsonNode byName = json(api.post(LIST, api.superAdmin(), "{\"term\":\"Owned Org\"}"));
        JsonNode byEmail = json(api.post(LIST, api.superAdmin(), "{\"term\":\"closed@example\"}"));

        assertThat(idsOf(byName)).containsExactly(ORG_OWNED);
        assertThat(idsOf(byEmail)).containsExactly(ORG_CLOSED);
    }

    @Test
    void theStatusFilterExcludesEveryOtherStatus() {
        JsonNode closed = json(api.post(LIST, api.superAdmin(), "{\"status\":\"CLOSED\"}"));

        assertThat(idsOf(closed)).contains(ORG_CLOSED).doesNotContain(ORG_A, ORG_LIFECYCLE);
    }

    /** A blank term is normalised to no filter rather than searched for. */
    @Test
    void aBlankTermListsEverything() {
        JsonNode blank = json(api.post(LIST, api.superAdmin(), "{\"term\":\"   \"}"));

        assertThat(idsOf(blank)).contains(ORG_A, ORG_LIFECYCLE);
    }

    @Test
    void anUnknownOrganizationIsAFourOhFour() {
        expect(asOperator(forOrg(FIND_ONE, UNKNOWN_ORG)), HttpStatus.NOT_FOUND);
    }

    @Test
    void findOneAnswersTheOrganizationsOwnRow() {
        JsonNode org = json(asOperator(forOrg(FIND_ONE, ORG_OWNED)));

        assertThat(idOf(org, ID)).isEqualTo(ORG_OWNED);
        assertThat(org.get("ownerUserId").asString()).isEqualTo(ORG_OWNER_USER_ID);
    }

    @Test
    void renamingRecordsTheNewName() {
        String name = slug("Renamed");

        JsonNode renamed = json(api.post(query(forOrg(RENAME, ORG_LIFECYCLE), param(NAME, name)), api.superAdmin(), null));

        assertThat(renamed.get(NAME).asString()).isEqualTo(name);
        assertThat(json(asOperator(forOrg(FIND_ONE, ORG_LIFECYCLE))).get(NAME).asString()).isEqualTo(name);
    }

    @Test
    void suspendingThenResumingReturnsTheOrganizationToActive() {
        expect(api.post(forOrg(SUSPEND, ORG_LIFECYCLE), api.superAdmin(), null), HttpStatus.OK);
        assertThat(json(asOperator(forOrg(FIND_ONE, ORG_LIFECYCLE))).get(STATUS).asString()).isEqualTo("SUSPENDED");

        JsonNode resumed = json(api.post(forOrg(RESUME, ORG_LIFECYCLE), api.superAdmin(), null));

        assertThat(resumed.get(STATUS).asString()).isEqualTo(ACTIVE);
    }

    /** Asking for the status an organization already holds is tolerated rather than refused. */
    @Test
    void resumingAnActiveOrganizationIsANoOp() {
        JsonNode resumed = json(api.post(forOrg(RESUME, ORG_A), api.superAdmin(), null));

        assertThat(resumed.get(STATUS).asString()).isEqualTo(ACTIVE);
    }

    @Test
    void closedIsTerminal() {
        expect(api.post(forOrg(RESUME, ORG_CLOSED), api.superAdmin(), null),
                HttpStatus.UNPROCESSABLE_CONTENT);
        expect(api.post(forOrg(SUSPEND, ORG_CLOSED), api.superAdmin(), null),
                HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    void closingAnOrganizationIsRefusedToItsOwnAdmin() {
        expect(api.post(forOrg(CLOSE, ORG_A), api.orgAdmin(ORG_A), null), HttpStatus.FORBIDDEN);
    }

    /**
     * The password is set on the account {@code manager_org.owner_user_id} names, not on the organization id — the
     * bug this endpoint carried for three releases was passing the latter where uaa wanted the former.
     */
    @Test
    void changingThePasswordResolvesTheOwnerFromTheOrganization() throws Exception {
        expect(api.post(forOrg(CHANGE_PASSWORD, ORG_OWNED), api.superAdmin(),
                NEW_PASSWORD), HttpStatus.OK);

        ArgumentCaptor<UserPassword> password = ArgumentCaptor.forClass(UserPassword.class);
        verify(userAccountService).changePassword(eq(ORG_OWNER_USER_ID), password.capture());
        assertThat(password.getValue().getChangePassword()).isEqualTo("new-secret");
    }

    @Test
    void anOrganizationWithNoRecordedOwnerAnswersAnUnprocessableEntity() {
        expect(api.post(forOrg(CHANGE_PASSWORD, ORG_A), api.superAdmin(),
                NEW_PASSWORD), HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    void creatingAnOrganizationAlsoCreatesItsFirstAdministratorInUaa() throws Exception {
        ReadableUser created = new ReadableUser();
        created.setId("created-user-id");
        when(userAccountService.createUser(any())).thenReturn(created);
        String email = String.format("%s@example.com", slug("founder"));

        expect(api.post(CREATE, api.superAdmin(), signUpBody(email, "correct-horse-8")), HttpStatus.OK);

        ArgumentCaptor<PersistableUser> user = ArgumentCaptor.forClass(PersistableUser.class);
        verify(userAccountService).createUser(user.capture());
        assertThat(user.getValue().getRoles()).containsExactly("ORG_ADMIN");
        assertThat(user.getValue().getUserName()).isEqualTo(email);
        assertThat(user.getValue().getOrg()).isNotBlank();
    }

    /**
     * The same {@code @Valid} that guards public signup guards this one — they share {@code CreateOrgRequest} and
     * {@code SignupService}, so an operator cannot create an organization the form rules would have refused. The
     * old body on this very test ({@code password: "pw"}, no names at all) is what used to be accepted.
     */
    @Test
    void creatingAnOrganizationWithAPasswordTheStoreWouldNotAcceptIsRefused() {
        expect(api.post(CREATE, api.superAdmin(), signUpBody("founder@example.com", "pw")),
                HttpStatus.BAD_REQUEST);
    }

    private static String signUpBody(String email, String password) {
        return String.format("""
                {"user":{"firstName":"Ada","lastName":"Lovelace","emailAddress":"%s",\
                "password":"%s","repeatPassword":"%s"}}""", email, password, password);
    }

    @Test
    void anOrganizationsStoresAreListedByItsId() {
        JsonNode stores = json(asOperator(forOrg(STORES, ORG_A)));

        assertThat(idsOf(stores)).contains("65f023632bc46470c104b76f", "65f023632bc46470c104b75f");
    }

    @Test
    void anOrganizationAdminMayNotListAnotherOrganizationsStores() {
        expect(api.get(forOrg(STORES, ORG_A), api.orgAdmin(ORG_A)), HttpStatus.FORBIDDEN);
    }

    @Test
    void anOrganizationWithNoStoresAnswersAnEmptyPage() {
        JsonNode stores = json(asOperator(forOrg(STORES, ORG_CLOSED)));

        assertThat(stores.get(CONTENT)).isEmpty();
    }

}
