package com.asrevo.cvhome.tenancy.manager.controller.statistic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import com.asrevo.cvhome.tenancy.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.ORG_A;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.expect;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.json;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The platform's growth curves.
 *
 * <p>
 * Both endpoints are aggregates over every organization — business metrics for the operator, not tenant data, and
 * not scopeable to one org. <strong>Neither carried an annotation</strong>, so any authenticated merchant could read
 * how fast the platform was growing; the refusals below are what stops that coming back.
 * </p>
 *
 * <p>
 * The store query is also the one whose schema qualifier the rename left as {@code manager.}, a schema that no
 * longer exists — so it would have failed the first time the statistics screen was opened. Only a live query
 * against Postgres catches that, which is why these are integration tests rather than a repository slice.
 * </p>
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class TenancyStatisticApiIntegrationTest {

    private static final String ORG_STATISTIC = "/api/v2/private/org-statistic";

    private static final String STORE_STATISTIC = "/api/v2/private/store-statistic";

    /** A range that spans every seeded row, whose {@code created_date} is the day the container came up. */
    private static final String RANGE = """
            {"fromDate":"2000-01-01T00:00:00.000+00:00","toDate":"2100-01-01T00:00:00.000+00:00"}""";

    private static final String ENTRIES = "entries";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    private TenancyApiTestSupport api;

    @BeforeEach
    void setUp() {
        api = new TenancyApiTestSupport(port, signer);
    }

    @Test
    void theOperatorReadsOrganizationsCreatedPerDay() {
        var response = api.post(ORG_STATISTIC, api.superAdmin(), RANGE);

        expect(response, HttpStatus.OK);
        assertThat(json(response).get(ENTRIES)).isNotEmpty();
    }

    @Test
    void theOperatorReadsStoresCreatedPerDay() {
        var response = api.post(STORE_STATISTIC, api.superAdmin(), RANGE);

        expect(response, HttpStatus.OK);
        assertThat(json(response).get(ENTRIES)).isNotEmpty();
    }

    @Test
    void anOrganizationAdminMayNotReadThePlatformsGrowthCurve() {
        expect(api.post(ORG_STATISTIC, api.orgAdmin(ORG_A), RANGE), HttpStatus.FORBIDDEN);
        expect(api.post(STORE_STATISTIC, api.orgAdmin(ORG_A), RANGE), HttpStatus.FORBIDDEN);
    }

    @Test
    void aStoreAdminMayNotEither() {
        expect(api.post(STORE_STATISTIC, api.storeAdmin(ORG_A, Tokens.STORE_1), RANGE), HttpStatus.FORBIDDEN);
    }

    @Test
    void anUnauthenticatedCallerIsRefused() {
        expect(api.post(ORG_STATISTIC, null, RANGE), HttpStatus.UNAUTHORIZED);
    }

}
