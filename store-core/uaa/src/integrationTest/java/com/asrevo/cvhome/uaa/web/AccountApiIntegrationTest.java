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
 * Self-service: my sessions, and a password change that signs my other sessions out.
 */
@DatabaseIntegrationTest
class AccountApiIntegrationTest {

    private static final String ACCOUNT = "org1-store2-admin";

    private static final String SESSIONS = "/api/v1/account/sessions";

    private static final String PASSWORD = "/api/v1/account/password";

    private static final String NEW_PASSWORD = "Fresh-Passw0rd-2026";

    private static final String CODE = "code";

    @LocalServerPort
    private int port;

    @Test
    void twoSessionsThenAPasswordChangeLeavesOnlyMine() throws IOException, InterruptedException {
        UaaClient first = new UaaClient(port);
        UaaClient second = new UaaClient(port);
        first.login(ACCOUNT, UaaClient.PASSWORD);
        second.login(ACCOUNT, UaaClient.PASSWORD);

        HttpResponse<String> listed = first.session(UaaClient.GET, SESSIONS, null);
        assertThat(listed.statusCode()).isEqualTo(200);
        JsonNode sessions = UaaClient.body(listed);
        assertThat(sessions.size()).isEqualTo(2);
        assertThat(sessions.findValues("current").stream().filter(JsonNode::asBoolean).count()).isEqualTo(1);
        assertThat(sessions.get(0).get("ip").asText()).isNotBlank();
        assertThat(sessions.get(0).get("via").asText()).isEqualTo("PASSWORD");

        HttpResponse<String> wrong = first.session(UaaClient.PUT, PASSWORD,
                String.format("{\"currentPassword\":\"nope\",\"newPassword\":\"%s\"}", NEW_PASSWORD));
        assertThat(wrong.statusCode()).isEqualTo(400);
        assertThat(UaaClient.body(wrong).get(CODE).asText()).isEqualTo("UAA.PASSWORD.CURRENT_MISMATCH");

        HttpResponse<String> weak = first.session(UaaClient.PUT, PASSWORD,
                String.format("{\"currentPassword\":\"%s\",\"newPassword\":\"short\"}", UaaClient.PASSWORD));
        assertThat(weak.statusCode()).isEqualTo(400);
        assertThat(UaaClient.body(weak).get(CODE).asText()).isEqualTo("UAA.PASSWORD.POLICY_VIOLATION");

        HttpResponse<String> changed = first.session(UaaClient.PUT, PASSWORD,
                String.format("{\"currentPassword\":\"%s\",\"newPassword\":\"%s\"}", UaaClient.PASSWORD, NEW_PASSWORD));
        assertThat(changed.statusCode()).as(changed.body()).isEqualTo(200);

        assertThat(first.session(UaaClient.GET, UaaClient.ME, null).statusCode()).isEqualTo(200);
        assertThat(second.session(UaaClient.GET, UaaClient.ME, null).statusCode()).isEqualTo(401);

        UaaClient third = new UaaClient(port);
        assertThat(UaaClient.location(third.login(ACCOUNT, UaaClient.PASSWORD))).contains("error");
        assertThat(UaaClient.location(third.login(ACCOUNT, NEW_PASSWORD))).endsWith("/");

        HttpResponse<String> serviceClient = first.bearer(UaaClient.GET, SESSIONS, null, first.storeCoreToken());
        assertThat(serviceClient.statusCode()).isEqualTo(403);
    }

}
