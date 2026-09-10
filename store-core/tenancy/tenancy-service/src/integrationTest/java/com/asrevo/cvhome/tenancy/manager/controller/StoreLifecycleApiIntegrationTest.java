package com.asrevo.cvhome.tenancy.manager.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import com.asrevo.cvhome.tenancy.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.DELETED_STORE;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.ORG_A;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.ORG_B;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.OPERATOR_CLOSED_STORE;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.ORG_LIFECYCLE;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.SUSPENDED_STORE;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.expect;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.json;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.query;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Opening and closing a store, over real HTTP.
 *
 * <p>
 * Suspend and resume are the operator's lever and are super-admin only — suspending takes a merchant's business
 * offline, which is not something their own admin should be able to do to themselves by accident, nor something they
 * can undo unilaterally. Archive and delete belong to the owner — the org admin whose organization owns the store,
 * or the operator — and carry {@code STORE-CORE.STORE-DELETE}. The permission cases below walk every other principal
 * the platform mints up to that gate: the store's own admin and moderator and a bare store-core service principal are
 * refused with 403, an admin of another organization gets the 404 tenancy answers for a store it does not own, and
 * the owning org admin and the operator get through.
 * </p>
 *
 * <p>
 * The transitions that succeed act on {@code STORE_3} (the owner) and {@code OPERATOR_CLOSED_STORE} (the operator),
 * which no other class reads, because DELETED is terminal. The refusals act on {@code STORE_3} too: a gate that
 * refuses never looks at the row, so its state is irrelevant to them.
 * </p>
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class StoreLifecycleApiIntegrationTest {

    private static final String BASE = "/api/v1/store-manager/private/store";

    private static final String STORE_PARAM = "store=";

    /** ORG_B's second store: this class moves it all the way to DELETED, so nothing else may depend on it. */
    private static final String STORE = Tokens.STORE_3;

    private static final String STATUS = "status";

    private static final String SUSPEND = "/suspend";

    private static final String RESUME = "/resume";

    private static final String ARCHIVE = "/archive";

    private static final String ACTIVE = "ACTIVE";

    private static final String ARCHIVED = "ARCHIVED";

    private static final String DELETED = "DELETED";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    private TenancyApiTestSupport api;

    @BeforeEach
    void setUp() {
        api = new TenancyApiTestSupport(port, signer);
    }

    private static String on(String path, String store) {
        return query(BASE + path, STORE_PARAM + store);
    }

    private String owner() {
        return api.orgAdmin(ORG_B);
    }

    @Test
    void anOrganizationAdminMayNotSuspendTheirOwnStore() {
        expect(api.post(on(SUSPEND, STORE), owner(), null), HttpStatus.FORBIDDEN);
    }

    @Test
    void anUnauthenticatedCallerIsRefused() {
        expect(api.post(on(SUSPEND, STORE), null, null), HttpStatus.UNAUTHORIZED);
    }

    @Test
    void theOperatorSuspendsAndResumesAStore() {
        JsonNode suspended = json(api.post(query(on(SUSPEND, SUSPENDED_STORE), "reason=unpaid"),
                api.superAdmin(), null));

        assertThat(suspended.get(STATUS).asString()).isEqualTo("SUSPENDED");

        JsonNode resumed = json(api.post(on(RESUME, SUSPENDED_STORE), api.superAdmin(), null));

        assertThat(resumed.get(STATUS).asString()).isEqualTo(ACTIVE);
    }

    /** Asking for the state a store is already in is what a double-click looks like, so it is tolerated. */
    @Test
    void resumingAnActiveStoreIsANoOp() {
        JsonNode resumed = json(api.post(on(RESUME, SUSPENDED_STORE), api.superAdmin(), null));

        assertThat(resumed.get(STATUS).asString()).isEqualTo(ACTIVE);
    }

    /**
     * The shared gate admits any org admin on this service ({@code store-ownership: DELEGATED}); the ownership check
     * is tenancy's own and answers 404, so asking to close somebody else's store does not confirm it exists.
     */
    @Test
    void aStoreOfAnotherOrganizationIsAFourOhFourRatherThanAForbidden() {
        expect(api.post(on(ARCHIVE, SUSPENDED_STORE), api.orgAdmin(ORG_B), null), HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.DELETE, on("", STORE), api.orgAdmin(ORG_A), null), HttpStatus.NOT_FOUND);
    }

    /** The operator closes a store of any organization; the gate admits the super admin beside the owner. */
    @Test
    void theOperatorArchivesAndDeletesAStoreOfAnyOrganization() {
        JsonNode archived = json(api.post(on(ARCHIVE, OPERATOR_CLOSED_STORE), api.superAdmin(), null));
        assertThat(archived.get(STATUS).asString()).isEqualTo(ARCHIVED);

        JsonNode deleted = json(api.send(HttpMethod.DELETE, on("", OPERATOR_CLOSED_STORE), api.superAdmin(), null));

        assertThat(deleted.get(STATUS).asString()).isEqualTo(DELETED);
    }

    /**
     * The store's own admin runs the store; closing it is the organization's decision. This is the case the read
     * token got wrong: a store admin passes {@code STORE-FIND-ONE} for their own store.
     */
    @Test
    void theStoresOwnAdminMayNotArchiveOrDeleteIt() {
        String storeAdmin = api.storeAdmin(ORG_B, STORE);

        expect(api.post(on(ARCHIVE, STORE), storeAdmin, null), HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.DELETE, on("", STORE), storeAdmin, null), HttpStatus.FORBIDDEN);
    }

    /**
     * The shared {@code store_core} client reads any store, which is what every store-core service needs; nothing
     * in store-core closes a store on a merchant's behalf, so the delete gate does not admit it.
     */
    @Test
    void aStoreCoreServicePrincipalMayNotArchiveOrDelete() {
        expect(api.post(on(ARCHIVE, STORE), api.service(), null), HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.DELETE, on("", STORE), api.service(), null), HttpStatus.FORBIDDEN);
    }

    /** Soft delete: the row and its id survive so billing's subscription and the registry's placement are not orphaned. */
    @Test
    void archivingThenDeletingLeavesTheRowInPlace() {
        JsonNode archived = json(api.post(on(ARCHIVE, STORE), owner(), null));
        assertThat(archived.get(STATUS).asString()).isEqualTo(ARCHIVED);

        JsonNode deleted = json(api.send(HttpMethod.DELETE, on("", STORE), owner(), null));

        assertThat(deleted.get(STATUS).asString()).isEqualTo(DELETED);
        assertThat(deleted.get("name").asString()).isEqualTo("ORG2-STORE2");
    }

    /** There is deliberately no path back from DELETED to anything. */
    @Test
    void deletedIsTerminal() {
        expect(api.post(on(ARCHIVE, DELETED_STORE), api.orgAdmin(ORG_LIFECYCLE), null),
                HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    void aStoreThatDoesNotExistIsAFourOhFour() {
        expect(api.post(on(ARCHIVE, "0000000000000000000000ff"), api.superAdmin(), null), HttpStatus.NOT_FOUND);
    }

    /**
     * Refused by role, not by store: a moderator of this very store is turned away, where the read token used to let
     * them through. The moderator of another store is refused too, and for the same reason now.
     */
    @Test
    void aModeratorMayNotDeleteAStoreWhetherTheirOwnOrAnother() {
        expect(api.send(HttpMethod.DELETE, on("", STORE), api.storeModerator(ORG_B, STORE), null),
                HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.DELETE, on("", Tokens.STORE_2), api.storeModerator(ORG_A, Tokens.STORE_1), null),
                HttpStatus.FORBIDDEN);
    }

}
