package com.asrevo.cvhome.uaa.web;

import java.io.IOException;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.uaa.support.UaaClient;
import com.fasterxml.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An administrator sees an account's sessions, ends one, and disabling the account ends the rest and its tokens.
 */
@DatabaseIntegrationTest
class AdminSessionsIntegrationTest {

    private static final String ACCOUNT = "org2-store1-moderator";

    private static final String ACCOUNT_ID = "f900b31e-376e-4757-8eab-501aba2cfdd3";

    private static final String USERS = "/api/v1/admin/users/";

    private static final String ONE_SESSION = "%s%s/sessions/%s";

    @LocalServerPort
    private int port;

    @Test
    void revokeOneThenDisableEndsAll() throws IOException, InterruptedException {
        UaaClient admin = new UaaClient(port);
        String token = admin.superAdminToken();
        UaaClient one = new UaaClient(port);
        UaaClient two = new UaaClient(port);
        one.login(ACCOUNT, UaaClient.PASSWORD);
        two.login(ACCOUNT, UaaClient.PASSWORD);

        HttpResponse<String> listed = admin.bearer(UaaClient.GET, String.format("%s%s/sessions", USERS, ACCOUNT_ID), null, token);
        JsonNode sessions = UaaClient.body(listed);
        assertThat(sessions.size()).isEqualTo(2);
        String firstId = sessions.get(0).get("id").asText();

        assertThat(admin.bearer(UaaClient.DELETE, String.format(ONE_SESSION, USERS, ACCOUNT_ID, firstId), null, token)
                .statusCode()).isEqualTo(200);
        int alive = 0;
        for (UaaClient c : new UaaClient[] {one, two}) {
            if (c.session(UaaClient.GET, UaaClient.ME, null).statusCode() == 200) {
                alive++;
            }
        }
        assertThat(alive).isEqualTo(1);

        HttpResponse<String> unknown = admin.bearer(UaaClient.DELETE, String.format(ONE_SESSION, USERS, ACCOUNT_ID, "nope"),
                null, token);
        assertThat(unknown.statusCode()).isEqualTo(404);

        assertThat(admin.bearer(UaaClient.POST, String.format("%s%s/disable", USERS, ACCOUNT_ID), null, token).statusCode())
                .isEqualTo(200);
        assertThat(one.session(UaaClient.GET, UaaClient.ME, null).statusCode()).isEqualTo(401);
        assertThat(two.session(UaaClient.GET, UaaClient.ME, null).statusCode()).isEqualTo(401);
        UaaClient again = new UaaClient(port);
        assertThat(UaaClient.location(again.login(ACCOUNT, UaaClient.PASSWORD))).endsWith("/login?error=disabled");

        assertThat(admin.bearer(UaaClient.POST, String.format("%s%s/enable", USERS, ACCOUNT_ID), null, token).statusCode())
                .isEqualTo(200);
    }

}
