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
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.billing.api.BillingApiSupport.LIFECYCLE_STORE;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.NEIGHBOUR_STORE;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.ORG_A;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.ORG_B;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.SUBSCRIPTION_STORE;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.V1;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.expect;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.json;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.path;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.scoped;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * A store's own subscription, over real HTTP.
 *
 * <p>
 * Three things are checked for every endpoint, because billing is both store- and org-scoped and each is a different
 * way to get it wrong: the happy path, the same call with a token from the neighbouring org, and the same call with
 * a role that may read but not spend. Reading and managing are separate permissions on purpose — a store moderator
 * should be able to see the plan they work under, and spending the org's money belongs to whoever owns the card.
 * </p>
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class SubscriptionApiIntegrationTest {

    private static final String CURRENT = path(V1, "subscription", "current");

    private static final String CHECKOUT = path(V1, "subscription", "checkout");

    private static final String PLAN = path(V1, "subscription", "plan");

    private static final String CANCEL = path(V1, "subscription", "cancel");

    private static final String RESUME = path(V1, "subscription", "resume");

    private static final String STATUS = "status";

    private static final String PLAN_PRICE_BODY = "{\"planPriceId\":\"%s\"}";

    private static final String CANCEL_BODY = "{\"immediate\":%s}";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    @Autowired
    private BillingFixtures fixtures;

    private BillingApiSupport api;

    private String orgAdmin;

    private String neighbourAdmin;

    private PlanPriceEntity cheapest;

    private PlanPriceEntity dearest;

    @BeforeEach
    void setUp() {
        api = new BillingApiSupport(port, signer);
        orgAdmin = api.orgAdmin(ORG_A);
        neighbourAdmin = api.orgAdmin(ORG_B);
        fixtures.publishPrices();
        cheapest = fixtures.cheapestPrice();
        dearest = fixtures.dearestPrice();
    }

    private ResponseEntity<String> currentAs(String store, String token) {
        return api.get(scoped(CURRENT, store), token);
    }

    /**
     * The raw id out of a value-object field. Every identifier on the wire is an object — {@code {"id": "..."}} —
     * because {@code PlanPriceId} is a value object rather than a bare string.
     */
    private static String priceIdOf(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value.isObject() ? value.get("id").asString() : value.asString();
    }

    private static String priceBody(PlanPriceEntity price) {
        return String.format(PLAN_PRICE_BODY, price.getId().getId());
    }

    // ---------------------------------------------------------------------------------------------- read

    @Test
    @DisplayName("an org admin reads the store's own subscription")
    void readsTheSubscription() {
        fixtures.active(SUBSCRIPTION_STORE, dearest);

        ResponseEntity<String> response = currentAs(SUBSCRIPTION_STORE, orgAdmin);

        expect(response, HttpStatus.OK);
        JsonNode body = json(response);
        assertThat(body.get(STATUS).asString()).isEqualTo(SubscriptionStatus.ACTIVE.name());
        assertThat(body.get("providerLinked").asBoolean()).isTrue();
        assertThat(body.get("entitlements")).isNotNull();
    }

    @Test
    @DisplayName("a store moderator may see the plan it works under")
    void aModeratorMayRead() {
        fixtures.active(SUBSCRIPTION_STORE, dearest);

        expect(api.get(scoped(CURRENT, SUBSCRIPTION_STORE),
                api.storeModerator(SUBSCRIPTION_STORE, ORG_A)), HttpStatus.OK);
    }

    @Test
    @DisplayName("the neighbouring org's admin cannot read this store's subscription")
    void anotherOrgCannotRead() {
        fixtures.active(SUBSCRIPTION_STORE, dearest);

        // The permission layer admits an org admin for any store — StoreRoleAccessChecker has no way to map a store
        // to its org and says so in a TODO — so what refuses this is billing's own query, narrowed by the org on
        // the subscription row. 404 rather than 403 is the shape of that: the row is simply not reachable.
        expect(currentAs(SUBSCRIPTION_STORE, neighbourAdmin), HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("this org's admin cannot read the neighbour's store either — the boundary holds both ways")
    void cannotReachTheNeighbour() {
        expect(currentAs(NEIGHBOUR_STORE, orgAdmin), HttpStatus.NOT_FOUND);
        // And the row really is there, so the refusal above is a refusal rather than an absence.
        assertThat(fixtures.read(NEIGHBOUR_STORE)).isNotNull();
    }

    @Test
    @DisplayName("a store admin of another store in the same org is refused")
    void aStoreAdminIsConfinedToItsStore() {
        // Same tenant, wrong store: the permission evaluator matches the token's store against the requested one.
        expect(api.get(scoped(CURRENT, SUBSCRIPTION_STORE),
                api.storeAdmin(LIFECYCLE_STORE, ORG_A)), HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("a request with no token at all is refused")
    void anonymousIsRefused() {
        expect(currentAs(SUBSCRIPTION_STORE, null), HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("a customer's token is not a staff token")
    void aCustomerIsRefused() {
        String customer = api.send(org.springframework.http.HttpMethod.GET, scoped(CURRENT, SUBSCRIPTION_STORE),
                new Tokens(signer).staff("ROLE_CUSTOMER", SUBSCRIPTION_STORE), null).getStatusCode().toString();

        assertThat(customer).startsWith("403");
    }

    // ------------------------------------------------------------------------------------------ checkout

    @Test
    @DisplayName("an org admin opens a checkout and is given somewhere to send the customer")
    void opensACheckout() {
        fixtures.pending(SUBSCRIPTION_STORE);

        ResponseEntity<String> response = api.post(scoped(CHECKOUT, SUBSCRIPTION_STORE), orgAdmin,
                priceBody(dearest));

        expect(response, HttpStatus.OK);
        assertThat(json(response).get("url").asString())
                .isEqualTo(ExternalClientsTestConfiguration.CHECKOUT_URL);
        // Still unpaid: the subscription becomes real when Stripe says the money moved, not when the redirect is
        // handed out.
        assertThat(fixtures.read(SUBSCRIPTION_STORE).getStatus()).isEqualTo(SubscriptionStatus.PENDING);
    }

    @Test
    @DisplayName("a store admin may read the plan but may not start a purchase")
    void aStoreAdminMayNotSpend() {
        fixtures.pending(SUBSCRIPTION_STORE);

        // MANAGE is the org admin's, because spending is an org-level act and a store admin is not the person who
        // owns the card.
        expect(api.post(scoped(CHECKOUT, SUBSCRIPTION_STORE), api.storeAdmin(SUBSCRIPTION_STORE, ORG_A),
                priceBody(dearest)), HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("the neighbouring org's admin cannot open a checkout against this store")
    void anotherOrgCannotCheckOut() {
        fixtures.pending(SUBSCRIPTION_STORE);

        expect(api.post(scoped(CHECKOUT, SUBSCRIPTION_STORE), neighbourAdmin, priceBody(dearest)),
                HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("a price that is not in the catalog is a 404")
    void anUnknownPriceIsNotFound() {
        fixtures.pending(SUBSCRIPTION_STORE);

        expect(api.post(scoped(CHECKOUT, SUBSCRIPTION_STORE), orgAdmin,
                String.format(PLAN_PRICE_BODY, "000000000000000000000000")), HttpStatus.NOT_FOUND);
    }

    // --------------------------------------------------------------------------------------- plan change

    @Test
    @DisplayName("a move to a dearer plan is applied at once and the response says so")
    void upgradeAppliesNow() {
        fixtures.active(SUBSCRIPTION_STORE, cheapest);

        ResponseEntity<String> response = api.post(scoped(PLAN, SUBSCRIPTION_STORE), orgAdmin, priceBody(dearest));

        expect(response, HttpStatus.OK);
        assertThat(priceIdOf(json(response), "planPriceId")).isEqualTo(dearest.getId().getId().toString());
        assertThat(json(response).get("pendingPlanChange").isNull()).isTrue();
        assertThat(fixtures.read(SUBSCRIPTION_STORE).getPlanPriceId()).isEqualTo(dearest.getId());
    }

    @Test
    @DisplayName("a move to a cheaper plan comes back pending, with the old plan still in force")
    void downgradeIsDeferred() {
        fixtures.active(SUBSCRIPTION_STORE, dearest);

        ResponseEntity<String> response = api.post(scoped(PLAN, SUBSCRIPTION_STORE), orgAdmin, priceBody(cheapest));

        expect(response, HttpStatus.OK);
        JsonNode body = json(response);
        // The direction is not the caller's to choose, and the response is how the console knows which happened.
        assertThat(priceIdOf(body, "planPriceId")).isEqualTo(dearest.getId().getId().toString());
        assertThat(priceIdOf(body.get("pendingPlanChange"), "planPriceId"))
                .isEqualTo(cheapest.getId().getId().toString());
        assertThat(fixtures.read(SUBSCRIPTION_STORE).getPendingPlanPriceId()).isEqualTo(cheapest.getId());
    }

    @Test
    @DisplayName("a store that never bought anything has nothing to move")
    void aPendingStoreCannotChangePlan() {
        fixtures.pending(SUBSCRIPTION_STORE);

        // There is nothing at Stripe to act on, which is a state problem rather than a missing row.
        expect(api.post(scoped(PLAN, SUBSCRIPTION_STORE), orgAdmin, priceBody(dearest)),
                HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    @DisplayName("the neighbouring org's admin cannot change this store's plan")
    void anotherOrgCannotChangePlan() {
        fixtures.active(SUBSCRIPTION_STORE, cheapest);

        expect(api.post(scoped(PLAN, SUBSCRIPTION_STORE), neighbourAdmin, priceBody(dearest)),
                HttpStatus.NOT_FOUND);
        // And nothing moved.
        assertThat(fixtures.read(SUBSCRIPTION_STORE).getPlanPriceId()).isEqualTo(cheapest.getId());
    }

    @Test
    @DisplayName("a store admin may not change the plan")
    void aStoreAdminMayNotChangePlan() {
        fixtures.active(SUBSCRIPTION_STORE, cheapest);

        expect(api.post(scoped(PLAN, SUBSCRIPTION_STORE), api.storeAdmin(SUBSCRIPTION_STORE, ORG_A),
                priceBody(dearest)), HttpStatus.FORBIDDEN);
    }

    // ------------------------------------------------------------------------------------ cancel, resume

    @Test
    @DisplayName("cancelling switches renewal off and leaves the store running")
    void cancelIsDeferredToThePeriodEnd() {
        fixtures.active(LIFECYCLE_STORE, dearest);

        ResponseEntity<String> response = api.post(scoped(CANCEL, LIFECYCLE_STORE), orgAdmin,
                String.format(CANCEL_BODY, false));

        expect(response, HttpStatus.OK);
        JsonNode body = json(response);
        assertThat(body.get("cancelAtPeriodEnd").asBoolean()).isTrue();
        // Not a cancellation: the customer keeps everything until the period they paid for runs out.
        assertThat(body.get(STATUS).asString()).isEqualTo(SubscriptionStatus.ACTIVE.name());
    }

    @Test
    @DisplayName("an org admin may not end a subscription immediately")
    void immediateCancelIsReservedForTheOperator() {
        fixtures.active(LIFECYCLE_STORE, dearest);

        // Taking away something already bought is not something a customer should be able to do to themselves by
        // accident, so the branch exists for the platform operator alone.
        expect(api.post(scoped(CANCEL, LIFECYCLE_STORE), orgAdmin, String.format(CANCEL_BODY, true)),
                HttpStatus.FORBIDDEN);
        assertThat(fixtures.read(LIFECYCLE_STORE).getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    @DisplayName("the platform operator may end a subscription immediately")
    void theOperatorMayCancelNow() {
        fixtures.active(LIFECYCLE_STORE, dearest);

        expect(api.post(scoped(CANCEL, LIFECYCLE_STORE), api.superAdmin(), String.format(CANCEL_BODY, true)),
                HttpStatus.OK);
        assertThat(fixtures.read(LIFECYCLE_STORE).getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
    }

    @Test
    @DisplayName("resuming a cancelled renewal switches it back on")
    void resumeSwitchesRenewalBackOn() {
        fixtures.active(LIFECYCLE_STORE, dearest);
        expect(api.post(scoped(CANCEL, LIFECYCLE_STORE), orgAdmin, String.format(CANCEL_BODY, false)),
                HttpStatus.OK);

        ResponseEntity<String> response = api.post(scoped(RESUME, LIFECYCLE_STORE), orgAdmin, null);

        expect(response, HttpStatus.OK);
        assertThat(json(response).get("cancelAtPeriodEnd").asBoolean()).isFalse();
    }

    @Test
    @DisplayName("resuming a subscription with only a pending downgrade calls the downgrade off")
    void resumeCallsOffAPendingDowngrade() {
        fixtures.active(LIFECYCLE_STORE, dearest);
        expect(api.post(scoped(PLAN, LIFECYCLE_STORE), orgAdmin, priceBody(cheapest)), HttpStatus.OK);

        ResponseEntity<String> response = api.post(scoped(RESUME, LIFECYCLE_STORE), orgAdmin, null);

        // The regression: revokeScheduledCancel refuses a subscription that was never cancelled, so this path used
        // to answer 422 with the schedule already released at Stripe — and the local pending change survived, for
        // the safety-net job to apply later.
        expect(response, HttpStatus.OK);
        assertThat(json(response).get("pendingPlanChange").isNull()).isTrue();
        assertThat(fixtures.read(LIFECYCLE_STORE).getPendingPlanPriceId()).isNull();
    }

    @Test
    @DisplayName("resuming a subscription with nothing scheduled is refused")
    void nothingToResume() {
        fixtures.active(LIFECYCLE_STORE, dearest);

        expect(api.post(scoped(RESUME, LIFECYCLE_STORE), orgAdmin, null), HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    @DisplayName("the neighbouring org's admin cannot cancel this store's subscription")
    void anotherOrgCannotCancel() {
        fixtures.active(LIFECYCLE_STORE, dearest);

        expect(api.post(scoped(CANCEL, LIFECYCLE_STORE), neighbourAdmin, String.format(CANCEL_BODY, false)),
                HttpStatus.NOT_FOUND);
        assertThat(fixtures.read(LIFECYCLE_STORE).isCancelAtPeriodEnd()).isFalse();
    }

    @Test
    @DisplayName("a store admin may not cancel")
    void aStoreAdminMayNotCancel() {
        fixtures.active(LIFECYCLE_STORE, dearest);

        expect(api.post(scoped(CANCEL, LIFECYCLE_STORE), api.storeAdmin(LIFECYCLE_STORE, ORG_A),
                String.format(CANCEL_BODY, false)), HttpStatus.FORBIDDEN);
    }

}
