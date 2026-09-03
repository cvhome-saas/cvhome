package com.asrevo.cvhome.uaa.invitation;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.uaa.support.UaaClient;
import com.fasterxml.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An administrator-issued reset link: used once, it sets the password and signs the account out everywhere.
 * Leaves {@code org1-store1-moderator} on the new password until the database is reset.
 */
@DatabaseIntegrationTest
class PasswordResetLinkIntegrationTest {

    private static final String ACCOUNT = "org1-store1-moderator";

    private static final String NEW_PASSWORD = "Reset-Passw0rd-2026";

    private static final String PREVIEW = "/api/v1/public/password-resets/%s";

    private static final String ACCEPT = "/api/v1/public/password-resets/%s/accept";

    private static final String TOKEN_PARAM = "token=";

    private static final String ERROR = "error";

    @LocalServerPort
    private int port;

    private UaaClient uaa;

    @BeforeEach
    void setUp() {
        uaa = new UaaClient(port);
    }

    @Test
    void theLinkResetsThePasswordOnceAndEndsTheOldSession() throws Exception {
        UaaClient victim = new UaaClient(port);
        assertThat(UaaClient.location(victim.login(ACCOUNT, UaaClient.PASSWORD))).doesNotContain(ERROR);
        assertThat(victim.session(UaaClient.GET, UaaClient.ME, null).statusCode()).isEqualTo(200);

        String admin = uaa.superAdminToken();
        JsonNode found = UaaClient.body(uaa.bearer(UaaClient.GET, String.format("/api/v1/admin/users?q=%s", ACCOUNT), null, admin));
        String userId = found.get("content").get(0).get("id").asText();
        HttpResponse<String> issued = uaa.bearer(UaaClient.POST,
                String.format("/api/v1/admin/users/%s/password-reset-links", userId), "{\"revokeSessions\": false}", admin);
        assertThat(issued.statusCode()).as(issued.body()).isEqualTo(201);
        assertThat(issued.body()).doesNotContain("\"invitation\":{");
        String url = UaaClient.body(issued).get("link").asText();
        String token = url.substring(url.indexOf(TOKEN_PARAM) + TOKEN_PARAM.length());

        // Issuing the link alone changed nothing for the signed-in person.
        assertThat(victim.session(UaaClient.GET, UaaClient.ME, null).statusCode()).isEqualTo(200);

        HttpResponse<String> preview = uaa.anonymous(UaaClient.GET, String.format(PREVIEW, token));
        assertThat(preview.statusCode()).isEqualTo(200);
        assertThat(UaaClient.body(preview).get("kind").asText()).isEqualTo("PASSWORD_RESET");
        assertThat(UaaClient.body(preview).get("username").asText()).isEqualTo(ACCOUNT);

        String body = String.format("{\"password\": \"%s\"}", NEW_PASSWORD);
        String accept = String.format(ACCEPT, token);
        HttpResponse<String> accepted = uaa.anonymous(UaaClient.POST, accept, body);
        assertThat(accepted.statusCode()).as(accepted.body()).isEqualTo(200);

        // Spent, the old session is gone, the old password is dead, the new one works.
        assertThat(uaa.anonymous(UaaClient.POST, accept, body).statusCode()).isEqualTo(404);
        assertThat(victim.session(UaaClient.GET, UaaClient.ME, null).statusCode()).isEqualTo(401);
        UaaClient again = new UaaClient(port);
        assertThat(UaaClient.location(again.login(ACCOUNT, UaaClient.PASSWORD))).contains(ERROR);
        assertThat(UaaClient.location(again.login(ACCOUNT, NEW_PASSWORD))).doesNotContain(ERROR);
    }

    @Test
    void theSuperAdminGetsNoResetLink() throws Exception {
        String admin = uaa.superAdminToken();

        HttpResponse<String> refused = uaa.bearer(UaaClient.POST,
                "/api/v1/admin/users/65d8419c-8765-4b8b-a15f-910dce959931/password-reset-links", null, admin);

        assertThat(refused.statusCode()).isEqualTo(403);
        assertThat(refused.body()).contains("UAA.USER.SUPER_ADMIN_IMMUTABLE");
    }

}
