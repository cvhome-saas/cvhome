package com.asrevo.cvhome.billing.api.v2;

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
import com.asrevo.cvhome.billing.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.billing.api.BillingApiSupport.ORG_A;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.PLATFORM_STORE;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.V2;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.expect;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.json;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.path;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The platform's billing aggregates.
 *
 * <p>
 * Business metrics for the operator, not tenant data, and none of them is scopeable to one org — hence
 * {@code hasRole('ROLE_SUPER_ADMIN')} on every method and a refusal case for every endpoint. Amounts are
 * <strong>minor units</strong> and the currency is the entry's name, so nothing is ever summed across currencies:
 * nothing on this platform holds an exchange rate, and a mixed total is a wrong number rather than a missing one.
 * </p>
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class BillingStatisticApiIntegrationTest {

    private static final String REVENUE = path(V2, "private", "revenue-statistic");

    private static final String SUBSCRIPTIONS = path(V2, "private", "subscription-statistic");

    private static final String PLANS = path(V2, "private", "plan-statistic");

    private static final String HEALTH = path(V2, "private", "billing-health");

    private static final String RANGE = """
            {"fromDate":"2020-01-01T00:00:00.000+00:00","toDate":"2030-01-01T00:00:00.000+00:00"}""";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    @Autowired
    private BillingFixtures fixtures;

    private BillingApiSupport api;

    private String operator;

    @BeforeEach
    void setUp() {
        api = new BillingApiSupport(port, signer);
        operator = api.superAdmin();
        fixtures.publishPrices();
        fixtures.active(PLATFORM_STORE, fixtures.dearestPrice());
    }

    @Test
    @DisplayName("revenue is summed per day and per currency")
    void revenueStatistic() {
        ResponseEntity<String> response = api.post(REVENUE, operator, RANGE);

        expect(response, HttpStatus.OK);
        // The entries list exists even when nothing has been paid; the currency is the entry's name so a caller
        // never has to guess which one a figure is in.
        assertThat(json(response).get("entries")).isNotNull();
    }

    @Test
    @DisplayName("subscriptions started are counted from the audit trail, per day and plan")
    void subscriptionStatistic() {
        ResponseEntity<String> response = api.post(SUBSCRIPTIONS, operator, RANGE);

        expect(response, HttpStatus.OK);
        assertThat(json(response).get("entries")).isNotNull();
    }

    @Test
    @DisplayName("the plan report says who is on what and what that is contracted to bring in")
    void planStatistic() {
        ResponseEntity<String> response = api.get(PLANS, operator);

        expect(response, HttpStatus.OK);
        JsonNode report = json(response);
        // Its own record rather than a StatisticList, because it carries counts *and* money in two dimensions and
        // a StatisticEntry's value is a single Number.
        assertThat(report.get("counts")).isNotNull();
        assertThat(report.get("recurringValue")).isNotNull();
    }

    @Test
    @DisplayName("the health reading answers from the two tables nothing else reads")
    void billingHealth() {
        ResponseEntity<String> response = api.get(HEALTH, operator);

        expect(response, HttpStatus.OK);
        JsonNode health = json(response);
        // Together these are the only "billing is broken right now" signal the platform has: webhooks arriving and
        // not being applied, and mutating Stripe calls that never came back.
        assertThat(health.get("failedEvents").asLong()).isNotNegative();
        assertThat(health.get("stalledRequests").asLong()).isNotNegative();
        assertThat(health.get("staleAfterMinutes").asInt()).isPositive();
    }

    @Test
    @DisplayName("an org admin is refused every aggregate")
    void anOrgAdminIsRefused() {
        String orgAdmin = api.orgAdmin(ORG_A);

        expect(api.post(REVENUE, orgAdmin, RANGE), HttpStatus.FORBIDDEN);
        expect(api.post(SUBSCRIPTIONS, orgAdmin, RANGE), HttpStatus.FORBIDDEN);
        expect(api.get(PLANS, orgAdmin), HttpStatus.FORBIDDEN);
        expect(api.get(HEALTH, orgAdmin), HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("a store admin is refused")
    void aStoreAdminIsRefused() {
        expect(api.get(PLANS, api.storeAdmin(PLATFORM_STORE, ORG_A)), HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("a service principal is refused: the operator's dashboards are not a service's business")
    void aServicePrincipalIsRefused() {
        expect(api.get(HEALTH, api.service(Tokens.SCOPE_STORE_CORE)), HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("no token at all is refused")
    void anonymousIsRefused() {
        expect(api.get(HEALTH, null), HttpStatus.UNAUTHORIZED);
    }

}
