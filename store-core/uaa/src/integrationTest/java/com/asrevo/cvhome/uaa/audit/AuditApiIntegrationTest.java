package com.asrevo.cvhome.uaa.audit;

import java.io.IOException;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.uaa.support.UaaClient;
import com.fasterxml.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The audit log as an API: what a sign-in writes, what the filters answer, what the export contains, and who may
 * ask. The rows come from real actions rather than fixtures — an audit test that inserts its own rows proves
 * nothing about whether the code records.
 */
@DatabaseIntegrationTest
class AuditApiIntegrationTest {

    private static final String AUDIT = "/api/v1/admin/audit";

    private static final String CONTENT = "content";

    private static final String EVENT_TYPE = "eventType";

    private static final String LOGIN_FAILED = "user.login.failed";

    private static final String TOTAL = "totalElements";

    private static final String ID = "id";

    private static final String ONE = "%s/%d";

    private static final String COUNTS = "counts";

    private static final String USERS = "users";

    private static final String DASHBOARD = "/api/v1/admin/dashboard";

    @LocalServerPort
    private int port;

    private UaaClient uaa;

    @BeforeEach
    void setUp() {
        uaa = new UaaClient(port);
    }

    @Test
    void aFailedSignInIsRecordedAndFilterable() throws IOException, InterruptedException {
        uaa.clearCookies();
        uaa.login(UaaClient.ORG1_ADMIN, "definitely-not-the-password");

        JsonNode page = UaaClient.body(uaa.bearer(UaaClient.GET,
                String.format("%s?type=%s&size=5", AUDIT, LOGIN_FAILED), null, uaa.superAdminToken()));
        assertThat(page.get(TOTAL).asLong()).isPositive();
        JsonNode first = page.get(CONTENT).get(0);
        assertThat(first.get(EVENT_TYPE).asText()).isEqualTo(LOGIN_FAILED);
        assertThat(first.get("outcome").asText()).isEqualTo("FAILURE");
        assertThat(first.get("category").asText()).isEqualTo("AUTHENTICATION");
        assertThat(first.get("targetName").asText()).isEqualTo(UaaClient.ORG1_ADMIN);
        assertThat(first.get("ip").asText()).isNotBlank();

        // Newest first, and one event reads on its own.
        long id = first.get(ID).asLong();
        JsonNode one = UaaClient.body(uaa.bearer(UaaClient.GET, String.format(ONE, AUDIT, id), null,
                uaa.superAdminToken()));
        assertThat(one.get(EVENT_TYPE).asText()).isEqualTo(LOGIN_FAILED);
        assertThat(uaa.bearer(UaaClient.GET, String.format(ONE, AUDIT, 99_999_999L), null, uaa.superAdminToken())
                .statusCode()).isEqualTo(404);
    }

    @Test
    void issuingATokenIsRecordedAgainstTheClient() throws IOException, InterruptedException {
        uaa.superAdminToken();
        JsonNode page = UaaClient.body(uaa.bearer(UaaClient.GET,
                String.format("%s?type=token.issued&size=5", AUDIT), null, uaa.superAdminToken()));
        assertThat(page.get(TOTAL).asLong()).isPositive();
        JsonNode first = page.get(CONTENT).get(0);
        assertThat(first.get("actorType").asText()).isEqualTo("CLIENT");
        assertThat(first.get("clientId").asText()).isNotBlank();
        assertThat(first.get("detail").asText()).contains("scopes=");
    }

    @Test
    void categoriesTypesAndTheRangeCheck() throws IOException, InterruptedException {
        String token = uaa.superAdminToken();
        JsonNode types = UaaClient.body(uaa.bearer(UaaClient.GET, String.format("%s/types", AUDIT), null, token));
        assertThat(types.size()).isGreaterThan(20);
        assertThat(types.toString()).contains(LOGIN_FAILED).contains("\"category\"");

        assertThat(uaa.bearer(UaaClient.GET, String.format("%s?category=AUTHENTICATION&size=1", AUDIT), null, token)
                .statusCode()).isEqualTo(200);

        // The console joins lists with commas rather than repeating the parameter; both must bind.
        uaa.clearCookies();
        uaa.login(UaaClient.ORG1_ADMIN, "still-not-the-password");
        JsonNode commaJoined = UaaClient.body(uaa.bearer(UaaClient.GET,
                String.format("%s?type=%s,user.login&size=5", AUDIT, LOGIN_FAILED), null, token));
        assertThat(commaJoined.get(TOTAL).asLong()).isPositive();
        for (JsonNode event : commaJoined.get(CONTENT)) {
            assertThat(event.get(EVENT_TYPE).asText()).isIn(LOGIN_FAILED, "user.login");
        }

        HttpResponse<String> backwards = uaa.bearer(UaaClient.GET,
                String.format("%s?from=2030-01-01T00:00:00Z&to=2020-01-01T00:00:00Z", AUDIT), null, token);
        assertThat(backwards.statusCode()).isEqualTo(400);
        assertThat(backwards.body()).contains("UAA.AUDIT.QUERY_INVALID").contains("\"field\":\"from\"");
    }

    @Test
    void theExportIsCsvAndTheGateHolds() throws IOException, InterruptedException {
        HttpResponse<String> csv = uaa.bearer(UaaClient.GET, String.format("%s/export?type=%s", AUDIT, LOGIN_FAILED),
                null, uaa.superAdminToken());
        assertThat(csv.statusCode()).isEqualTo(200);
        assertThat(csv.headers().firstValue("content-type").orElse("")).contains("text/csv");
        assertThat(csv.headers().firstValue("content-disposition").orElse("")).contains("uaa-audit.csv");
        assertThat(csv.body().lines().findFirst().orElse(""))
                .isEqualTo("\"occurredAt\",\"eventType\",\"outcome\",\"reasonCode\",\"actorType\",\"actorId\","
                        + "\"actorName\",\"targetType\",\"targetId\",\"targetName\",\"clientId\",\"ip\",\"detail\","
                        + "\"traceId\"");

        assertThat(uaa.bearer(UaaClient.GET, AUDIT, null, uaa.storeCoreToken()).statusCode()).isEqualTo(403);
        assertThat(uaa.anonymous(UaaClient.GET, AUDIT).statusCode()).isEqualTo(401);
    }

    @Test
    void theDashboardCountsWhatTheTablesHold() throws IOException, InterruptedException {
        String token = uaa.superAdminToken();
        HttpResponse<String> response = uaa.bearer(UaaClient.GET, String.format("%s?range=24h", DASHBOARD), null, token);
        assertThat(response.statusCode()).as(response.body()).isEqualTo(200);
        JsonNode dashboard = UaaClient.body(response);
        assertThat(dashboard.get("range").asText()).isEqualTo("24h");
        assertThat(dashboard.get("tokensIssued").asLong()).isPositive();
        assertThat(dashboard.get(COUNTS).get(USERS).asLong()).isPositive();
        assertThat(dashboard.get(COUNTS).get("roles").asLong()).isPositive();
        assertThat(dashboard.get(COUNTS).get("clients").asLong()).isPositive();
        assertThat(dashboard.get(USERS).get("total").asLong()).isPositive();

        // Every posture line is computed, and each says which check it is and how it stands.
        JsonNode posture = dashboard.get("posture");
        assertThat(posture.size()).isGreaterThanOrEqualTo(6);
        for (JsonNode check : posture) {
            assertThat(check.get(ID).asText()).isNotBlank();
            assertThat(check.get("level").asText()).isIn("OK", "WARN", "RISK");
        }

        assertThat(uaa.bearer(UaaClient.GET, DASHBOARD, null, uaa.storeCoreToken()).statusCode())
                .isEqualTo(403);
    }

}
