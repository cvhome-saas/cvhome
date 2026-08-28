package com.asrevo.cvhome.billing.api.v1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.asrevo.cvhome.billing.api.BillingApiSupport;
import com.asrevo.cvhome.billing.api.BillingFixtures;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.billing.api.BillingApiSupport.NEIGHBOUR_STORE;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.ORG_A;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.ORG_B;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.V1;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.expect;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.json;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.path;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.scoped;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The two service-to-service surfaces: what a store may do, and whether an org may open another.
 *
 * <p>
 * Both are wider than the human-facing endpoints, deliberately and in different directions. Entitlement reads admit
 * any store-pod principal, because the pods are what enforce the ceilings — routing that through
 * {@code isScopeStorePod}, which additionally requires the caller's resource to match a pod, denied every pod that
 * asked, since billing is a store-core service and has no pod. Quota checks admit the store-core scope alone,
 * because no human ever asks them.
 * </p>
 *
 * <p>
 * Each entitlement case uses a store of its own. {@code EntitlementServiceImpl} caches a snapshot for thirty
 * seconds, so two cases sharing a store would have the second read the first's answer — and the whole suite runs in
 * well under thirty seconds.
 * </p>
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class ExternalBillingApiIntegrationTest {

    private static final String ENTITLEMENTS = "entitlements";

    private static final String REASON = "reason";

    private static final String STATUS_FIELD = "status";

    private static final String TRIAL_AVAILABLE = "trialAvailable";

    private static final String ENTITLEMENT = "entitlement";

    private static final String PRIVATE = "private";

    private static final String SNAPSHOT_SEGMENT = "snapshot";

    private static final String QUOTA_SEGMENT = "quota";

    private static final String SNAPSHOT = path(V1, ENTITLEMENT, PRIVATE, SNAPSHOT_SEGMENT);

    private static final String BATCH = path(V1, ENTITLEMENT, PRIVATE, SNAPSHOT_SEGMENT, "batch");

    private static final String BLOCKED = path(V1, ENTITLEMENT, PRIVATE, "blocked-stores");

    private static final String QUOTA = path(V1, QUOTA_SEGMENT, PRIVATE, "store-create");

    private static final String PROVISION = path(V1, QUOTA_SEGMENT, PRIVATE, "provision");

    private static final String ORG_BODY = "{\"org\":\"%s\"}";

    private static final String PROVISION_BODY = "{\"org\":\"%s\",\"store\":\"%s\"}";

    private static final String OPERABLE = "operable";

    private static final String ALLOWED = "allowed";

    /** One store per case: the snapshot cache would otherwise carry one case's answer into the next. */
    private static final String PAYING_STORE = "b1110000000000000000cc01";

    private static final String UNPAID_STORE = "b1110000000000000000cc02";

    private static final String BLOCKED_STORE = "b1110000000000000000cc03";

    private static final String UNBLOCKED_STORE = "b1110000000000000000cc04";

    private static final String BATCH_STORE = "b1110000000000000000cc05";

    private static final String ROLE_STORE = "b1110000000000000000cc06";

    /** Never provisioned by the seed, so provisioning it here is a genuine first time. */
    private static final String FRESH_STORE = "b1110000000000000000cc07";

    /** An org nothing provisions, so its quota answer stays the untouched "allowed" one. */
    private static final String QUOTA_ORG = "52a034a43cd77581d105c87c";

    /** A second empty org, for the case that actually spends a trial — which QUOTA_ORG must not have done. */
    private static final String PROVISION_ORG = "62a034a43cd77581d105c87d";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    @Autowired
    private BillingFixtures fixtures;

    private BillingApiSupport api;

    private String storeCoreService;

    private String storePodService;

    @BeforeEach
    void setUp() {
        api = new BillingApiSupport(port, signer);
        storeCoreService = api.service(Tokens.SCOPE_STORE_CORE);
        storePodService = api.service(Tokens.SCOPE_STORE_POD);
        fixtures.publishPrices();
    }

    // ------------------------------------------------------------------------------------------ entitlements

    @Test
    @DisplayName("a paying store's snapshot says it may be worked in, and carries its plan's grants")
    void snapshotOfAPayingStore() {
        fixtures.active(PAYING_STORE, fixtures.dearestPrice());

        ResponseEntity<String> response = api.get(scoped(SNAPSHOT, PAYING_STORE), storePodService);

        expect(response, HttpStatus.OK);
        JsonNode snapshot = json(response);
        assertThat(snapshot.get(STATUS_FIELD).asString()).isEqualTo(SubscriptionStatus.ACTIVE.name());
        assertThat(snapshot.get(OPERABLE).asBoolean()).isTrue();
        assertThat(snapshot.get("planCode").asString()).isNotBlank();
        assertThat(snapshot.get(ENTITLEMENTS)).isNotEmpty();
    }

    @Test
    @DisplayName("an unpaid store's snapshot says it may not be worked in, and grants nothing")
    void snapshotOfAnUnpaidStore() {
        fixtures.pending(UNPAID_STORE);

        JsonNode snapshot = json(api.get(scoped(SNAPSHOT, UNPAID_STORE), storePodService));

        assertThat(snapshot.get(OPERABLE).asBoolean()).isFalse();
        // An empty grant map reads as "unlimited" in EntitlementSnapshot, which is why the status is what has to
        // gate such a store — and does.
        assertThat(snapshot.get(ENTITLEMENTS)).isEmpty();
    }

    @Test
    @DisplayName("a store-core service principal may read a snapshot too")
    void aStoreCoreServiceMayRead() {
        fixtures.active(BATCH_STORE, fixtures.dearestPrice());

        expect(api.get(scoped(SNAPSHOT, BATCH_STORE), storeCoreService), HttpStatus.OK);
    }

    @Test
    @DisplayName("any staff principal may read a snapshot, including one for another store")
    void theSnapshotIsDeliberatelyWide() {
        fixtures.active(ROLE_STORE, fixtures.dearestPrice());

        // Wider than SubscriptionApi's READ on purpose, and this is the shape of that width: a staff token carries
        // the store-pod scope, so hasAccessOnBillingEntitlementRead admits it without looking at the store at all.
        // Defensible because a snapshot carries nothing a store's own `current` would not — the plan code, whether
        // it is operable, and the ceilings — but it does mean the store in the query is not a boundary here.
        expect(api.get(scoped(SNAPSHOT, ROLE_STORE), api.storeAdmin(NEIGHBOUR_STORE, ORG_B)), HttpStatus.OK);
    }

    @Test
    @DisplayName("a batch read answers one snapshot per store that exists")
    void batchSnapshots() {
        fixtures.active(BATCH_STORE, fixtures.dearestPrice());

        ResponseEntity<String> response = api.post(BATCH, storeCoreService,
                String.format("[\"%s\",\"%s\"]", BATCH_STORE, NEIGHBOUR_STORE));

        expect(response, HttpStatus.OK);
        // Authorised on the scope rather than per store in the body: the callers render or guard many stores at
        // once, and checking each id would turn one call back into the N this endpoint exists to avoid.
        assertThat(json(response)).hasSize(2);
    }

    @Test
    @DisplayName("a batch containing a store that does not exist answers for the ones that do")
    void batchSkipsUnknownStores() {
        fixtures.active(BATCH_STORE, fixtures.dearestPrice());

        ResponseEntity<String> response = api.post(BATCH, storeCoreService,
                String.format("[\"%s\",\"b9990000000000000000ff99\"]", BATCH_STORE));

        // A caller rendering a list must not lose the whole page because one row has no subscription yet.
        expect(response, HttpStatus.OK);
        assertThat(json(response)).hasSize(1);
    }

    @Test
    @DisplayName("the blocked list is the gateway's once-a-minute question")
    void blockedStores() {
        fixtures.pending(BLOCKED_STORE);

        ResponseEntity<String> response = api.get(BLOCKED, storeCoreService);

        expect(response, HttpStatus.OK);
        assertThat(storeIds(json(response))).contains(BLOCKED_STORE);
    }

    @Test
    @DisplayName("an active store is not on the blocked list")
    void anActiveStoreIsNotBlocked() {
        fixtures.active(UNBLOCKED_STORE, fixtures.dearestPrice());

        assertThat(storeIds(json(api.get(BLOCKED, storeCoreService)))).doesNotContain(UNBLOCKED_STORE);
    }

    @Test
    @DisplayName("a snapshot for a store billing has never seen is a 404")
    void anUnknownStoreIsNotFound() {
        expect(api.get(scoped(SNAPSHOT, "b9990000000000000000ff98"), storeCoreService), HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("no token at all is refused")
    void anonymousIsRefused() {
        expect(api.get(scoped(SNAPSHOT, PAYING_STORE), null), HttpStatus.UNAUTHORIZED);
        expect(api.get(BLOCKED, null), HttpStatus.UNAUTHORIZED);
    }

    // ------------------------------------------------------------------------------------------------- quota

    @Test
    @DisplayName("an org holding no unpaid stores may open one, with its trial still to spend")
    void quotaAllows() {
        ResponseEntity<String> response = api.post(QUOTA, storeCoreService, String.format(ORG_BODY, QUOTA_ORG));

        expect(response, HttpStatus.OK);
        JsonNode decision = json(response);
        // A decision rather than an error, because the caller has to render *why* — and whether a trial is still on
        // the table changes what the next screen says.
        assertThat(decision.get(ALLOWED).asBoolean()).isTrue();
        assertThat(decision.get(REASON).isNull()).isTrue();
        assertThat(decision.get(TRIAL_AVAILABLE).asBoolean()).isTrue();
        assertThat(decision.get("pendingStoreCount").asInt()).isZero();
    }

    @Test
    @DisplayName("an org stockpiling unpaid stores is refused, and told which rule it hit")
    void quotaRefusesStockpiling() {
        // The seed gives org A more never-paid-for stores than max-pending-stores allows. Not a cap on stores it
        // may own — each store carries its own subscription and pays for itself.
        JsonNode decision = json(api.post(QUOTA, storeCoreService, String.format(ORG_BODY, ORG_A)));

        assertThat(decision.get(ALLOWED).asBoolean()).isFalse();
        assertThat(decision.get(REASON).asString()).isEqualTo("TOO_MANY_PENDING_STORES");
    }

    @Test
    @DisplayName("an org that has spent its trial is told so")
    void quotaReportsASpentTrial() {
        JsonNode decision = json(api.post(QUOTA, storeCoreService, String.format(ORG_BODY, ORG_B)));

        // The seed records org B's trial grant, so the answer differs from org A's on this field alone.
        assertThat(decision.get(TRIAL_AVAILABLE).asBoolean()).isFalse();
    }

    @Test
    @DisplayName("provisioning a store gives it a subscription, and a repeat changes nothing")
    void provisionIsIdempotent() {
        String body = String.format(PROVISION_BODY, PROVISION_ORG, FRESH_STORE);

        ResponseEntity<String> first = api.post(PROVISION, storeCoreService, body);
        expect(first, HttpStatus.OK);
        String firstStatus = json(first).get(STATUS_FIELD).asString();
        // The org's first store, so it wins the trial claim.
        assertThat(firstStatus).isEqualTo(SubscriptionStatus.TRIALING.name());

        // It arrives from an outbox handler, so a repeat is routine rather than exceptional — and a second claim
        // would be a second chance at the org's one trial.
        ResponseEntity<String> second = api.post(PROVISION, storeCoreService, body);
        expect(second, HttpStatus.OK);
        assertThat(json(second).get(STATUS_FIELD).asString()).isEqualTo(firstStatus);
    }

    @Test
    @DisplayName("a pod's token cannot ask a quota question")
    void aPodIsRefusedTheQuota() {
        // A service-to-service call from store-core only: no human ever asks this, and a pod has no business
        // deciding whether an org may open a store.
        expect(api.post(QUOTA, storePodService, String.format(ORG_BODY, ORG_A)), HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("an org admin cannot ask a quota question either")
    void anOrgAdminIsRefusedTheQuota() {
        expect(api.post(QUOTA, api.orgAdmin(ORG_A), String.format(ORG_BODY, ORG_A)), HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("an org admin cannot provision a subscription")
    void anOrgAdminCannotProvision() {
        expect(api.post(PROVISION, api.orgAdmin(ORG_A), String.format(PROVISION_BODY, ORG_A, FRESH_STORE)),
                HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("no token at all cannot provision")
    void anonymousCannotProvision() {
        expect(api.post(PROVISION, null, String.format(PROVISION_BODY, ORG_A, FRESH_STORE)),
                HttpStatus.UNAUTHORIZED);
    }

    /** {@code StoreMerchantId} serialises as a bare string on this endpoint, not as a wrapped object. */
    private static java.util.List<String> storeIds(JsonNode array) {
        return array.valueStream().map(JsonNode::asString).toList();
    }

}
