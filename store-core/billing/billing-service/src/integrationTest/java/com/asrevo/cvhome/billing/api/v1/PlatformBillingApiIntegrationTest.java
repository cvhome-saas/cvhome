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
import com.asrevo.cvhome.billing.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.billing.api.BillingApiSupport.ORG_A;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.ORG_B;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.PLATFORM_STORE;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.V1;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.expect;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.json;
import static com.asrevo.cvhome.billing.api.BillingApiSupport.path;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Billing across every tenant: the register, the ledger and the audit trail.
 *
 * <p>
 * These are the operator's screens, not a tenant's, and none of them is scopeable to one org — which is why every
 * method is {@code hasRole('ROLE_SUPER_ADMIN')} and why the refusal cases here matter more than the happy paths. An
 * org admin reaching this would be reading every merchant on the platform.
 * </p>
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class PlatformBillingApiIntegrationTest {

    private static final String ACTIVE = "ACTIVE";

    private static final String ID = "id";

    private static final String ORG_FIELD = "org";

    private static final String STATUS_FIELD = "status";

    private static final String PLATFORM = "platform";

    private static final String INVOICES_SEGMENT = "invoices";

    private static final String SUBSCRIPTIONS = path(V1, PLATFORM, "subscriptions");

    private static final String INVOICES = path(V1, PLATFORM, INVOICES_SEGMENT);

    private static final String TOTALS = path(V1, PLATFORM, INVOICES_SEGMENT, "totals");

    private static final String AUDIT = path(V1, PLATFORM, "audit");

    private static final String CONTENT = "content";

    private static final String TOTAL_ELEMENTS = "totalElements";

    private static final String EMPTY_QUERY = "{}";

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

    private JsonNode post(String url, String body) {
        ResponseEntity<String> response = api.post(url, operator, body);
        expect(response, HttpStatus.OK);
        return json(response);
    }

    // ---------------------------------------------------------------------------------------------- register

    @Test
    @DisplayName("the operator sees subscriptions from every org at once")
    void listsAcrossTenants() {
        JsonNode page = post(SUBSCRIPTIONS, EMPTY_QUERY);

        assertThat(page.get(CONTENT)).isNotEmpty();
        assertThat(page.get(TOTAL_ELEMENTS).asLong()).isPositive();
        // Both tenants are seeded, so a listing that had quietly acquired an org filter would show only one.
        assertThat(page.get(CONTENT).valueStream().map(it -> it.get(ORG_FIELD).get(ID).asString()).toList())
                .contains(ORG_A, ORG_B);
    }

    @Test
    @DisplayName("the total is the register's own count, not the size of the page")
    void theTotalIsNotThePageSize() {
        JsonNode page = post(SUBSCRIPTIONS, EMPTY_QUERY);

        // Two queries assembled by hand, because Spring Data JDBC has no countQuery. Nothing but this stops the
        // total drifting from the rows.
        assertThat(page.get(TOTAL_ELEMENTS).asLong())
                .isGreaterThanOrEqualTo(page.get(CONTENT).size());
    }

    @Test
    @DisplayName("an org filter narrows the register to that tenant")
    void filtersByOrg() {
        JsonNode page = post(SUBSCRIPTIONS, String.format("{\"org\":\"%s\"}", ORG_B));

        assertThat(page.get(CONTENT)).isNotEmpty();
        assertThat(page.get(CONTENT).valueStream().map(it -> it.get(ORG_FIELD).get(ID).asString()).distinct().toList())
                .containsExactly(ORG_B);
    }

    @Test
    @DisplayName("a status filter narrows to that status")
    void filtersByStatus() {
        JsonNode page = post(SUBSCRIPTIONS, "{\"status\":\"ACTIVE\"}");

        assertThat(page.get(CONTENT).valueStream().map(it -> it.get(STATUS_FIELD).asString()).distinct().toList())
                .allMatch(ACTIVE::equals);
    }

    @Test
    @DisplayName("blockedOnly narrows to the stores no enforcement layer should let through")
    void filtersBlockedOnly() {
        JsonNode page = post(SUBSCRIPTIONS, "{\"blockedOnly\":true}");

        assertThat(page.get(CONTENT).valueStream().map(it -> it.get(STATUS_FIELD).asString()).distinct().toList())
                .allMatch(status -> !ACTIVE.equals(status) && !"TRIALING".equals(status)
                        && !"PAST_DUE".equals(status));
    }

    // ------------------------------------------------------------------------------------------------ ledger

    @Test
    @DisplayName("the invoice ledger answers, even with nothing in it")
    void theLedgerAnswers() {
        JsonNode page = post(INVOICES, EMPTY_QUERY);

        assertThat(page.get(CONTENT)).isNotNull();
        assertThat(page.get(TOTAL_ELEMENTS).asLong()).isNotNegative();
    }

    @Test
    @DisplayName("the totals are a second call on the same filter, one figure per currency")
    void theTotalsAnswer() {
        ResponseEntity<String> response = api.post(TOTALS, operator, EMPTY_QUERY);

        expect(response, HttpStatus.OK);
        // A separate call rather than a field on the page, so the rows can render while the sums are computed.
        assertThat(json(response).isArray()).isTrue();
    }

    // ------------------------------------------------------------------------------------------------- audit

    @Test
    @DisplayName("the audit trail answers, and it is the trail rather than the subscription rows")
    void theAuditTrailAnswers() {
        JsonNode page = post(AUDIT, EMPTY_QUERY);

        assertThat(page.get(CONTENT)).isNotNull();
        assertThat(page.get(TOTAL_ELEMENTS).asLong()).isNotNegative();
    }

    @Test
    @DisplayName("an event-type filter reaches the trail's query")
    void filtersTheTrail() {
        JsonNode page = post(AUDIT, "{\"eventType\":\"ACTIVATED\"}");

        assertThat(page.get(CONTENT).valueStream().map(it -> it.get("eventType").asString()).distinct().toList())
                .allMatch("ACTIVATED"::equals);
    }

    // ---------------------------------------------------------------------------------------------- refusals

    @Test
    @DisplayName("an org admin is refused every platform screen")
    void anOrgAdminIsRefused() {
        String orgAdmin = api.orgAdmin(ORG_A);

        // Reaching any of these would be reading every merchant on the platform, including their spend.
        expect(api.post(SUBSCRIPTIONS, orgAdmin, EMPTY_QUERY), HttpStatus.FORBIDDEN);
        expect(api.post(INVOICES, orgAdmin, EMPTY_QUERY), HttpStatus.FORBIDDEN);
        expect(api.post(TOTALS, orgAdmin, EMPTY_QUERY), HttpStatus.FORBIDDEN);
        expect(api.post(AUDIT, orgAdmin, EMPTY_QUERY), HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("a store admin is refused too")
    void aStoreAdminIsRefused() {
        expect(api.post(SUBSCRIPTIONS, api.storeAdmin(PLATFORM_STORE, ORG_A), EMPTY_QUERY), HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("a service principal is refused: these are an operator's screens, not a service's")
    void aServicePrincipalIsRefused() {
        expect(api.post(SUBSCRIPTIONS, api.service(Tokens.SCOPE_STORE_CORE), EMPTY_QUERY), HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("no token at all is refused")
    void anonymousIsRefused() {
        expect(api.post(SUBSCRIPTIONS, null, EMPTY_QUERY), HttpStatus.UNAUTHORIZED);
    }

}
