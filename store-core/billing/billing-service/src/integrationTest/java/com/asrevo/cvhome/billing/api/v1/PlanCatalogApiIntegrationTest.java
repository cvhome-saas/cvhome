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
import com.asrevo.cvhome.billing.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.billing.api.BillingApiSupport.V1;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.expect;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.json;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.path;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The public pricing page's data, and the catalog the seeder actually wrote.
 *
 * <p>
 * Deliberately the only unauthenticated endpoint besides the webhook: a pricing page is read by people who have not
 * signed up yet, so requiring a token would make the product unsellable. It is also the one end-to-end check that
 * {@code PlanCatalogSeeder} ran and produced the catalog {@code plan-catalog.yml} declares — every other test takes
 * that catalog as given.
 * </p>
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class PlanCatalogApiIntegrationTest {

    private static final String PLANS = path(V1, "plan", "public", "plans");

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    private BillingApiSupport api;

    @BeforeEach
    void setUp() {
        api = new BillingApiSupport(port, signer);
    }

    @Test
    @DisplayName("the plan catalog is readable without a token")
    void thePricingPageNeedsNoToken() {
        ResponseEntity<String> response = api.get(PLANS, null);

        expect(response, HttpStatus.OK);
        assertThat(json(response)).isNotEmpty();
    }

    @Test
    @DisplayName("the seeded plans come back cheapest tier first, each with prices and grants")
    void listsTheSeededCatalog() {
        JsonNode plans = json(api.get(PLANS, null));

        assertThat(plans).isNotEmpty();
        JsonNode first = plans.get(0);
        assertThat(first.get("code").asString()).isNotBlank();
        assertThat(first.get("displayName").asString()).isNotBlank();
        assertThat(first.get("prices")).isNotEmpty();
        assertThat(first.get("entitlements")).isNotNull();

        // Ordered by tier, which is what makes "is this an upgrade" answerable and what a pricing page renders in.
        int previous = Integer.MIN_VALUE;
        for (JsonNode plan : plans) {
            int tier = plan.get("tier").asInt();
            assertThat(tier).isGreaterThanOrEqualTo(previous);
            previous = tier;
        }
    }

    @Test
    @DisplayName("a currency filter narrows the prices without dropping any plan")
    void filtersByCurrency() {
        JsonNode all = json(api.get(PLANS, null));
        JsonNode usd = json(api.get(PLANS + "?currency=USD", null));

        assertThat(usd).hasSameSizeAs(all);
        for (JsonNode plan : usd) {
            for (JsonNode price : plan.get("prices")) {
                assertThat(price.get("amount").get("currency").get("code").asString()).isEqualTo("USD");
            }
        }
    }

    @Test
    @DisplayName("a currency nobody is priced in leaves every plan with no prices, not an error")
    void anUnknownCurrencyIsEmptyRatherThanMissing() {
        JsonNode plans = json(api.get(PLANS + "?currency=ZZZ", null));

        // The plan is still a plan; what to do with an empty price list is the pricing page's decision.
        assertThat(plans).isNotEmpty();
        for (JsonNode plan : plans) {
            assertThat(plan.get("prices")).isEmpty();
        }
    }

    @Test
    @DisplayName("a token does no harm on a public endpoint")
    void aTokenIsIgnored() {
        expect(api.get(PLANS, api.superAdmin()), HttpStatus.OK);
    }

}
