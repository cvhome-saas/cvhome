package com.asrevo.cvhome.tenancy.manager.controller;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import com.asrevo.cvhome.billing.commons.dto.StoreQuotaDecision;
import com.asrevo.cvhome.billing.services.entitlement.ExternalEntitlementService;
import com.asrevo.cvhome.billing.services.quota.ExternalStoreQuotaService;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.merchant.api.MerchantStorePodClient;
import com.asrevo.cvhome.podregistry.commons.dto.PlacementDecision;
import com.asrevo.cvhome.podregistry.services.placement.ExternalPodPlacementService;
import com.asrevo.cvhome.tenancy.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.tenancy.manager.service.StorePodClientFactory;
import com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.DELETED_STORE;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.ORG_A;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.ORG_B;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.ORG_CREATOR;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.POD_ID;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.expect;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.ID_FIELD;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.idOf;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.idsOf;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.json;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.path;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.with;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.slug;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * The store listing and creation surface over real HTTP.
 *
 * <p>
 * The property under test is that the rows are confined <em>in the query</em>, not by the permission gate: the
 * shared {@code StoreRoleAccessChecker.isOrgAdmin} ignores the store it is asked about, so a
 * {@code hasPermission(#store,…)} on its own admits any org admin to any store on the platform. Every listing case
 * below therefore repeats itself as a second organization's administrator and proves they see nothing of the first.
 * </p>
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class StoreManagerApiIntegrationTest {

    private static final String BASE = "/api/v1/store-manager";

    private static final String STORE_PARAM = "store";

    private static final String LIST = path(BASE, "list");

    private static final String PRIVATE_STORE = path(BASE, "private", STORE_PARAM);

    private static final String STORE_INFO = path(BASE, "store-info");

    private static final String STORES_PER_POD = path(BASE, "stores-per-pod");

    private static final String UNIQUE = path(PRIVATE_STORE, "unique");

    private static final String PUBLIC = path(BASE, "public");

    private static final String COLOR_THEMES = path(PUBLIC, "color-themes");

    private static final String NAME = "name";

    private static final String CODE = "code";

    private static final String STORE_CODE = "ORG1-STORE1";

    private static final String UNKNOWN_STORE = "0000000000000000000000ff";

    private static final String EXISTS = "exists";

    private static final String POD_ID_FIELD = "podId";

    private static final String EMPTY_FILTER = "{}";

    /** ORG_A's stores, as data.sql seeds them. */
    private static final String ORG_A_STORE_1 = Tokens.STORE_1;

    private static final String ORG_A_STORE_2 = Tokens.STORE_2;

    /** ORG_B's stores. */
    private static final String ORG_B_STORE_1 = Tokens.STORE_4;

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    @Autowired
    private ExternalStoreQuotaService quotaService;

    @Autowired
    private ExternalPodPlacementService placementService;

    @Autowired
    private ExternalEntitlementService entitlementService;

    @Autowired
    private StorePodClientFactory podClientFactory;

    private TenancyApiTestSupport api;

    @BeforeEach
    void setUp() {
        api = new TenancyApiTestSupport(port, signer);
        Mockito.reset(quotaService, placementService, entitlementService, podClientFactory);
    }

    private JsonNode listAs(String token) {
        var response = api.post(LIST, token, EMPTY_FILTER);
        expect(response, HttpStatus.OK);
        return json(response);
    }

    @Test
    void theOperatorSeesEveryOrganizationsStores() {
        assertThat(idsOf(listAs(api.superAdmin()))).contains(ORG_A_STORE_1, ORG_A_STORE_2, ORG_B_STORE_1);
    }

    @Test
    void anOrganizationAdminSeesOnlyTheirOwnOrganizationsStores() {
        assertThat(idsOf(listAs(api.orgAdmin(ORG_A)))).contains(ORG_A_STORE_1, ORG_A_STORE_2)
                .doesNotContain(ORG_B_STORE_1);
    }

    @Test
    void aSecondOrganizationsAdminSeesNoneOfTheFirstsStores() {
        assertThat(idsOf(listAs(api.orgAdmin(ORG_B)))).contains(ORG_B_STORE_1)
                .doesNotContain(ORG_A_STORE_1, ORG_A_STORE_2);
    }

    /** A store-level role is confined further still — to the one store its token names. */
    @Test
    void aStoreAdminSeesOnlyTheirOwnStore() {
        assertThat(idsOf(listAs(api.storeAdmin(ORG_A, ORG_A_STORE_1)))).containsExactly(ORG_A_STORE_1);
    }

    /**
     * Absence of a recognised role has to mean less access, never more. This listing keyed off
     * {@code isOrgAdminOrAnyStoreAdmin()}, so a principal holding some other role matched no branch and was handed
     * every store on the platform.
     */
    @Test
    void aPrincipalWithNoneOfTheViewerRolesIsRefused() {
        expect(api.post(LIST, api.customer(ORG_A, ORG_A_STORE_1), EMPTY_FILTER), HttpStatus.FORBIDDEN);
    }

    @Test
    void anUnauthenticatedCallerIsRefused() {
        expect(api.post(LIST, null, EMPTY_FILTER), HttpStatus.UNAUTHORIZED);
    }

    /** A soft-deleted store is gone as far as every listing is concerned. */
    @Test
    void aDeletedStoreIsLeftOutOfEveryListing() {
        assertThat(idsOf(listAs(api.superAdmin()))).doesNotContain(DELETED_STORE);
    }

    @Test
    void theNameFilterMatchesASubstringCaseInsensitively() {
        JsonNode page = json(api.post(LIST, api.superAdmin(), "{\"name\":\"org1-store\"}"));

        assertThat(idsOf(page)).containsExactlyInAnyOrder(ORG_A_STORE_1, ORG_A_STORE_2);
    }

    /** The pod filter narrows what the caller may already see; it never widens it. */
    @Test
    void thePodFilterComposesWithTheOrganizationScoping() {
        JsonNode page = json(api.post(LIST, api.orgAdmin(ORG_A), String.format("{\"pod\":{\"id\":\"%s\"}}", POD_ID)));

        assertThat(idsOf(page)).contains(ORG_A_STORE_1).doesNotContain(ORG_B_STORE_1);
    }

    @Test
    void theDetailedListingIsScopedTheSameWay() {
        JsonNode page = json(api.get(PRIVATE_STORE, api.orgAdmin(ORG_B)));

        assertThat(idsOf(page)).contains(ORG_B_STORE_1).doesNotContain(ORG_A_STORE_1);
    }

    @Test
    void storesPerPodIsAPlatformAggregateAndSoOperatorOnly() {
        var response = api.get(STORES_PER_POD, api.superAdmin());

        expect(response, HttpStatus.OK);
        assertThat(json(response).valueStream().map(it -> idOf(it, POD_ID_FIELD)).toList()).contains(POD_ID);
        expect(api.get(STORES_PER_POD, api.orgAdmin(ORG_A)), HttpStatus.FORBIDDEN);
    }

    @Test
    void aTakenStoreNameIsReportedAsTaken() {
        JsonNode taken = json(api.get(with(UNIQUE, NAME, STORE_CODE), api.superAdmin()));
        JsonNode free = json(api.get(with(UNIQUE, NAME, slug("brand-new")),
                api.superAdmin()));

        assertThat(taken.get(EXISTS).asBoolean()).isTrue();
        assertThat(free.get(EXISTS).asBoolean()).isFalse();
    }

    /**
     * Store names are unique platform-wide, so this necessarily reports on names outside the caller's org. It is
     * restricted to those who can actually create a store precisely so it cannot be used to enumerate them.
     */
    @Test
    void aStoreAdminMayNotProbeStoreNames() {
        expect(api.get(with(UNIQUE, NAME, STORE_CODE),
                api.storeAdmin(ORG_A, ORG_A_STORE_1)), HttpStatus.FORBIDDEN);
    }

    @Test
    void storeInfoAnswersTheCallersOwnStore() {
        JsonNode store = json(api.get(with(STORE_INFO, STORE_PARAM, ORG_A_STORE_1), api.orgAdmin(ORG_A)));

        assertThat(idOf(store, ID_FIELD)).isEqualTo(ORG_A_STORE_1);
        assertThat(store.get(NAME).asString()).isEqualTo(STORE_CODE);
    }

    /**
     * A foreign store raises the same 404 as a missing one. A 403 would confirm the id exists, which is how a
     * platform's store ids get enumerated by probing status codes.
     */
    @Test
    void aSecondOrganizationsAdminCannotReadTheFirstsStore() {
        expect(api.get(with(STORE_INFO, STORE_PARAM, ORG_A_STORE_1), api.orgAdmin(ORG_B)), HttpStatus.NOT_FOUND);
    }

    @Test
    void aStoreAdminCannotReadASiblingStoreInTheirOwnOrganization() {
        expect(api.get(with(STORE_INFO, STORE_PARAM, ORG_A_STORE_2), api.storeAdmin(ORG_A, ORG_A_STORE_1)),
                HttpStatus.FORBIDDEN);
    }

    @Test
    void storeInfoForAnUnknownStoreIsAFourOhFour() {
        expect(api.get(with(STORE_INFO, STORE_PARAM, UNKNOWN_STORE), api.superAdmin()),
                HttpStatus.NOT_FOUND);
    }

    /**
     * The detail comes from the store's pod, decorated with the pod reference the console's switcher needs.
     *
     * <p>
     * The reference is asserted by the pod's identifier rather than by its hex string on purpose: the controller
     * puts a bare {@code ObjectId} into the map rather than the {@code PodId} value object, so Jackson has no
     * {@code @JsonSerialize} to follow and writes {@code {"date":…,"timestamp":…}} instead of the 24-character id
     * every other endpoint answers with. Pinning the identity here keeps the test honest about which pod was
     * resolved without pretending the wire shape is what the console wants.
     * </p>
     */
    @Test
    void theStoreDetailIsFetchedFromItsPodAndCarriesThePodReference() {
        MerchantStorePodClient client = Mockito.mock(MerchantStorePodClient.class);
        when(podClientFactory.getMerchantStorePodClient(any())).thenReturn(client);
        when(client.getStore(ORG_A_STORE_1)).thenReturn(Map.of(CODE, STORE_CODE));

        JsonNode detail = json(api.get(path(PRIVATE_STORE, ORG_A_STORE_1), api.orgAdmin(ORG_A)));

        assertThat(detail.get(CODE).asString()).isEqualTo(STORE_CODE);
        assertThat(detail.get("pod").get("id").get("timestamp").asInt())
                .isEqualTo(new ObjectId(POD_ID).getTimestamp());
    }

    @Test
    void aSecondOrganizationsAdminCannotReadTheFirstsStoreDetail() {
        expect(api.get(path(PRIVATE_STORE, ORG_A_STORE_1), api.orgAdmin(ORG_B)), HttpStatus.NOT_FOUND);
    }

    @Test
    void creatingAStorePlacesItOnThePodTheRegistryChose() throws Exception {
        allowQuotaAndPlacement();
        String name = slug("NEW-STORE");

        var response = api.post(PRIVATE_STORE, api.orgAdmin(ORG_CREATOR), createBody(name));

        expect(response, HttpStatus.OK);
        JsonNode created = json(response);
        assertThat(created.get(NAME).asString()).isEqualTo(name);
        assertThat(idOf(created, POD_ID_FIELD)).isEqualTo(POD_ID);
        assertThat(created.get("provisioningState").asString()).isEqualTo("NOT_STARTED_PROVISIONING");
    }

    /**
     * The billing check runs before a pod is chosen, so a refusal costs nothing — and it fails closed: a store that
     * exists with nobody billed for it is not noticed until someone reconciles revenue.
     */
    @Test
    void billingRefusingTheQuotaStopsTheStoreBeingCreated() throws Exception {
        when(quotaService.checkStoreCreate(any())).thenReturn(StoreQuotaDecision.refuse("plan limit reached", false, 1));

        expect(api.post(PRIVATE_STORE, api.orgAdmin(ORG_CREATOR), createBody(slug("REFUSED"))),
                HttpStatus.UNPROCESSABLE_CONTENT);
    }

    /**
     * A body the pod would refuse is rejected here, synchronously, with field errors. Provisioning is asynchronous,
     * so without this the caller is told the store is being built and then that it failed, and at no point which
     * field was wrong.
     */
    @Test
    void aCreateMissingAFieldThePodRequiresIsRejectedUpFront() {
        expect(api.post(PRIVATE_STORE, api.orgAdmin(ORG_CREATOR),
                String.format("{\"name\":\"%s\"}", slug("HALF"))), HttpStatus.BAD_REQUEST);
    }

    @Test
    void aStoreNameAlreadyTakenIsAConflict() throws Exception {
        allowQuotaAndPlacement();

        expect(api.post(PRIVATE_STORE, api.orgAdmin(ORG_CREATOR), createBody(STORE_CODE)), HttpStatus.CONFLICT);
    }

    @Test
    void aStoreAdminMayNotCreateAStore() throws Exception {
        allowQuotaAndPlacement();

        expect(api.post(PRIVATE_STORE, api.storeAdmin(ORG_A, ORG_A_STORE_1), createBody(slug("NOPE"))),
                HttpStatus.FORBIDDEN);
    }

    @Test
    void theThemeCataloguesArePublic() {
        expect(api.get(path(PUBLIC, "themes"), null), HttpStatus.OK);
        expect(api.get(COLOR_THEMES, null), HttpStatus.OK);
        expect(api.get(path(PUBLIC, "social-links-providers"), null), HttpStatus.OK);
        assertThat(json(api.get(COLOR_THEMES, null))).isNotEmpty();
    }

    private void allowQuotaAndPlacement() throws Exception {
        when(quotaService.checkStoreCreate(any())).thenReturn(StoreQuotaDecision.allow(true, 0));
        when(placementService.place(any())).thenReturn(new PlacementDecision(new PodId(POD_ID), false, "seeded pod"));
        when(entitlementService.snapshots(any())).thenReturn(List.of());
    }

    private static String createBody(String name) {
        return String.format("""
                {"name":"%s","email":"owner@example.com","phone":"00201000000000","theme":"DEFAULT",
                 "colorTheme":"DEFAULT","currency":"EGP","defaultLanguage":"en","supportedLanguages":["en"],
                 "address":{"country":"EG","city":"Cairo","postalCode":"11311"}}""", name);
    }

}
