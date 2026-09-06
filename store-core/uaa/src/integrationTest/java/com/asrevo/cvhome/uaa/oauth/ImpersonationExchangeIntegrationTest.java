package com.asrevo.cvhome.uaa.oauth;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.uaa.support.UaaClient;
import com.fasterxml.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The impersonation grant against the real token endpoint: the seeded {@code console-impersonation} client, a real
 * operator token from the authorization-code flow, and the rows the exchange leaves behind.
 *
 * <p>
 * This is also the proof that our converter answers for {@code grant_type=token-exchange} ahead of Spring's own —
 * were the order reversed, the built-in converter would reject {@code requested_subject} as a malformed request and
 * every case here would answer 400.
 * </p>
 */
@DatabaseIntegrationTest
class ImpersonationExchangeIntegrationTest {

    private static final String TOKEN = "/oauth2/token";

    private static final String GRANT = "urn:ietf:params:oauth:grant-type:token-exchange";

    private static final String ACCESS_TOKEN_TYPE = "urn:ietf:params:oauth:token-type:access_token";

    private static final String ORG1_STORE1_ADMIN_ID = "60ab49a5-7f06-4b5a-be81-9b30bb6559ae";

    private static final String ORG1_STORE1 = "65f023632bc46470c104b76f";

    private static final String SUPER_ADMIN_ID = "65d8419c-8765-4b8b-a15f-910dce959931";

    private static final String REASON = "ticket 42";

    private static final String ACCESS_TOKEN = "access_token";

    private static final String ERROR = "error";

    private static final String AUDIT = "select count(*) from uaa.audit_events where event_type = ? and target_id = ?";

    private static final String READ = "read";

    private static final String WRITE = "write";

    private static final String STARTED = "user.impersonation.started";

    private static final String ENDED = "user.impersonation.ended";

    private static final String DENIED = "user.impersonation.denied";

    private static final String ACT = "act";

    private static final String ACT_MODE = "act_mode";

    private static final String SUB = "sub";

    private static final String UID = "uid";

    private static final String ROLES = "roles";

    private static final String STORE_ADMIN = "STORE_ADMIN";

    private static final String ACCESS_DENIED = "access_denied";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbc;

    private static Map<String, String> exchange(String subjectToken, String target, String store, String mode) {
        Map<String, String> form = new HashMap<>();
        form.put("grant_type", GRANT);
        form.put("subject_token", subjectToken);
        form.put("subject_token_type", ACCESS_TOKEN_TYPE);
        form.put("requested_token_type", ACCESS_TOKEN_TYPE);
        form.put("requested_subject", target);
        form.put("impersonation_store", store);
        form.put("impersonation_mode", mode);
        form.put("reason", REASON);
        return form;
    }

    private long audited(String type, String targetId) {
        Long n = jdbc.queryForObject(AUDIT, Long.class, type, targetId);
        return n == null ? 0 : n;
    }

    @Test
    void aSuperAdminActsAsAmerchantReadOnlyAndTheTrailSaysSo() throws Exception {
        UaaClient uaa = new UaaClient(port);
        String operator = uaa.userAccessToken(UaaClient.SUPER_ADMIN);
        long started = audited(STARTED, ORG1_STORE1_ADMIN_ID);
        long ended = audited(ENDED, ORG1_STORE1_ADMIN_ID);

        HttpResponse<String> response = uaa.clientPost(UaaClient.IMPERSONATION, UaaClient.LCL_SECRET, TOKEN,
                exchange(operator, ORG1_STORE1_ADMIN_ID, ORG1_STORE1, READ));

        assertThat(response.statusCode()).as(response.body()).isEqualTo(200);
        JsonNode body = UaaClient.body(response);
        assertThat(body.get("issued_token_type").asText()).isEqualTo(ACCESS_TOKEN_TYPE);
        assertThat(body.get(ACT_MODE).asText()).isEqualTo(READ);
        assertThat(body.has("refresh_token")).as("never a refresh token").isFalse();
        assertThat(body.get("expires_in").asInt()).isLessThanOrEqualTo(900);
        JsonNode claims = UaaClient.claims(body.get(ACCESS_TOKEN).asText());
        // sub is the merchant, so every store-scoped check on every service works unchanged...
        assertThat(claims.get(SUB).asText()).isEqualTo(ORG1_STORE1_ADMIN_ID);
        assertThat(claims.get(UID).asText()).isEqualTo(ORG1_STORE1_ADMIN_ID);
        assertThat(claims.get("org").asText()).isEqualTo("21f023932bc66470c104b76f");
        assertThat(claims.get("store").asText()).isEqualTo(ORG1_STORE1);
        // ...read mode narrows the roles to the platform's read-only store role...
        assertThat(claims.get(ROLES).toString()).contains("STORE_MODERATOR").doesNotContain(STORE_ADMIN);
        // ...and act names the operator.
        assertThat(claims.get(ACT).get(SUB).asText()).isEqualTo(UaaClient.SUPER_ADMIN);
        assertThat(claims.get(ACT).get(UID).asText()).isEqualTo(SUPER_ADMIN_ID);
        assertThat(claims.get(ACT_MODE).asText()).isEqualTo(READ);
        assertThat(audited(STARTED, ORG1_STORE1_ADMIN_ID)).isEqualTo(started + 1);

        // Ending it is a revocation, which the audit trail also records as the impersonation ending.
        HttpResponse<String> revoked = uaa.clientPost(UaaClient.IMPERSONATION, UaaClient.LCL_SECRET, "/oauth2/revoke",
                Map.of("token", body.get(ACCESS_TOKEN).asText()));
        assertThat(revoked.statusCode()).isEqualTo(200);
        assertThat(audited(ENDED, ORG1_STORE1_ADMIN_ID)).isEqualTo(ended + 1);
    }

    @Test
    void writeModeCarriesTheMerchantsOwnRoles() throws Exception {
        UaaClient uaa = new UaaClient(port);
        String operator = uaa.userAccessToken(UaaClient.SUPER_ADMIN);

        HttpResponse<String> response = uaa.clientPost(UaaClient.IMPERSONATION, UaaClient.LCL_SECRET, TOKEN,
                exchange(operator, ORG1_STORE1_ADMIN_ID, ORG1_STORE1, WRITE));

        assertThat(response.statusCode()).as(response.body()).isEqualTo(200);
        JsonNode claims = UaaClient.claims(UaaClient.body(response).get(ACCESS_TOKEN).asText());
        assertThat(claims.get(ROLES).toString()).contains(STORE_ADMIN);
        assertThat(claims.get(ACT).get(SUB).asText()).isEqualTo(UaaClient.SUPER_ADMIN);
    }

    @Test
    void anImpersonatedTokenCannotBeExchangedAgain() throws Exception {
        UaaClient uaa = new UaaClient(port);
        String operator = uaa.userAccessToken(UaaClient.SUPER_ADMIN);
        HttpResponse<String> first = uaa.clientPost(UaaClient.IMPERSONATION, UaaClient.LCL_SECRET, TOKEN,
                exchange(operator, ORG1_STORE1_ADMIN_ID, ORG1_STORE1, READ));
        String impersonated = UaaClient.body(first).get(ACCESS_TOKEN).asText();
        long denied = audited(DENIED, ORG1_STORE1_ADMIN_ID);

        HttpResponse<String> chained = uaa.clientPost(UaaClient.IMPERSONATION, UaaClient.LCL_SECRET, TOKEN,
                exchange(impersonated, ORG1_STORE1_ADMIN_ID, ORG1_STORE1, READ));

        assertThat(chained.statusCode()).isEqualTo(400);
        assertThat(UaaClient.body(chained).get(ERROR).asText()).isEqualTo("invalid_grant");
        assertThat(audited(DENIED, ORG1_STORE1_ADMIN_ID)).isEqualTo(denied + 1);
    }

    @Test
    void supportActsReadOnlyAndNeverInWriteMode() throws Exception {
        UaaClient uaa = new UaaClient(port);
        String operator = uaa.userAccessToken(UaaClient.SUPPORT);

        HttpResponse<String> write = uaa.clientPost(UaaClient.IMPERSONATION, UaaClient.LCL_SECRET, TOKEN,
                exchange(operator, ORG1_STORE1_ADMIN_ID, ORG1_STORE1, WRITE));
        HttpResponse<String> read = uaa.clientPost(UaaClient.IMPERSONATION, UaaClient.LCL_SECRET, TOKEN,
                exchange(operator, ORG1_STORE1_ADMIN_ID, ORG1_STORE1, READ));

        assertThat(write.statusCode()).isEqualTo(400);
        assertThat(UaaClient.body(write).get(ERROR).asText()).isEqualTo(ACCESS_DENIED);
        assertThat(read.statusCode()).as(read.body()).isEqualTo(200);
        assertThat(UaaClient.claims(UaaClient.body(read).get(ACCESS_TOKEN).asText()).get(ACT).get(SUB).asText())
                .isEqualTo(UaaClient.SUPPORT);
    }

    @Test
    void anOrgAdminMayNotImpersonateAndAplatformPrincipalCannotBeImpersonated() throws Exception {
        UaaClient uaa = new UaaClient(port);
        String orgAdmin = uaa.userAccessToken(UaaClient.ORG1_ADMIN);
        HttpResponse<String> byOrgAdmin = uaa.clientPost(UaaClient.IMPERSONATION, UaaClient.LCL_SECRET, TOKEN,
                exchange(orgAdmin, ORG1_STORE1_ADMIN_ID, ORG1_STORE1, READ));
        assertThat(byOrgAdmin.statusCode()).isEqualTo(400);
        assertThat(UaaClient.body(byOrgAdmin).get(ERROR).asText()).isEqualTo(ACCESS_DENIED);

        uaa.clearCookies();
        String operator = uaa.userAccessToken(UaaClient.SUPER_ADMIN);
        HttpResponse<String> ofSuperAdmin = uaa.clientPost(UaaClient.IMPERSONATION, UaaClient.LCL_SECRET, TOKEN,
                exchange(operator, SUPER_ADMIN_ID, ORG1_STORE1, READ));
        assertThat(ofSuperAdmin.statusCode()).isEqualTo(400);
        assertThat(UaaClient.body(ofSuperAdmin).get(ERROR).asText()).isEqualTo(ACCESS_DENIED);
    }

    /** The grant is the impersonation client's alone: web-app holds the operator's session, not this power. */
    @Test
    void theGrantIsRefusedToEveryOtherClient() throws Exception {
        UaaClient uaa = new UaaClient(port);
        String operator = uaa.userAccessToken(UaaClient.SUPER_ADMIN);

        HttpResponse<String> response = uaa.clientPost(UaaClient.WEB_APP, UaaClient.LCL_SECRET, TOKEN,
                exchange(operator, ORG1_STORE1_ADMIN_ID, ORG1_STORE1, READ));

        assertThat(response.statusCode()).isIn(400, 401);
        assertThat(UaaClient.body(response).get(ERROR).asText()).isIn("unauthorized_client", "invalid_client");
    }

}
