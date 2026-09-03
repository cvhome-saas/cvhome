package com.asrevo.cvhome.uaa.client;

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
 * The registry API: a secret is answered at creation and never read back; the list carries type and status; the
 * registration rules answer typed problems; the gate holds.
 */
@DatabaseIntegrationTest
class AdminClientApiIntegrationTest {

    private static final String CLIENT_SECRET = "clientSecret";

    /** Quoted: {@code status.clientSecretExpiresAt} legitimately contains the bare word. */
    private static final String SECRET_KEY = "\"clientSecret\"";

    private static final String CONTENT = "content";

    private static final String FIELD_ERRORS = "fieldErrors";

    private static final String STATS = "/stats";

    private static final String FIELD = "field";

    private static final String X = "x";

    @LocalServerPort
    private int port;

    private UaaClient uaa;

    @BeforeEach
    void setUp() {
        uaa = new UaaClient(port);
    }

    @Test
    void createAnswersTheSecretOnceAndReadsNeverDo() throws IOException, InterruptedException {
        String token = uaa.superAdminToken();
        JsonNode created = ClientApiSupport.register(uaa, "it-api-machine");
        String id = created.get(ClientApiSupport.CLIENT).get("id").asText();

        assertThat(created.get(CLIENT_SECRET).asText()).isNotBlank();
        assertThat(created.get(ClientApiSupport.CLIENT).get(ClientApiSupport.STATUS).get("type").asText()).isEqualTo("MACHINE");
        assertThat(created.get(ClientApiSupport.CLIENT).get(ClientApiSupport.STATUS).get("clientSecretExpiresAt").isNull())
                .isFalse();
        assertThat(created.get(ClientApiSupport.CLIENT).get(ClientApiSupport.STATUS).get("description").asText())
                .isEqualTo("integration test");

        HttpResponse<String> read = uaa.bearer(UaaClient.GET, ClientApiSupport.path(id), null, token);
        assertThat(read.statusCode()).isEqualTo(200);
        assertThat(read.body()).doesNotContain(SECRET_KEY).doesNotContain(created.get(CLIENT_SECRET).asText());

        HttpResponse<String> list = uaa.bearer(UaaClient.GET,
                String.format("%s?type=MACHINE&q=it-api&count=50", ClientApiSupport.CLIENTS), null, token);
        JsonNode rows = UaaClient.body(list).get(CONTENT);
        assertThat(rows.size()).isEqualTo(1);
        assertThat(rows.get(0).get("enabled").asBoolean()).isTrue();
        assertThat(rows.get(0).get("grantTypes").get(0).asText()).isEqualTo("client_credentials");

        HttpResponse<String> stats = uaa.bearer(UaaClient.GET, ClientApiSupport.CLIENTS + STATS, null, token);
        assertThat(UaaClient.body(stats).get("total").asLong()).isGreaterThanOrEqualTo(5);
        assertThat(UaaClient.body(stats).get("machine").asLong()).isGreaterThanOrEqualTo(3);

        assertThat(uaa.bearer(UaaClient.DELETE, ClientApiSupport.path(id), null, token).statusCode()).isEqualTo(200);
        assertThat(uaa.bearer(UaaClient.GET, ClientApiSupport.path(id), null, token).statusCode()).isEqualTo(404);
    }

    @Test
    void registrationRulesAnswerTypedProblems() throws IOException, InterruptedException {
        String token = uaa.superAdminToken();

        HttpResponse<String> taken = uaa.bearer(UaaClient.POST, ClientApiSupport.CLIENTS,
                ClientApiSupport.machine(UaaClient.ADMIN_SDK, "dup"), token);
        assertThat(taken.statusCode()).isEqualTo(409);
        assertThat(UaaClient.body(taken).get(ClientApiSupport.CODE).asText()).isEqualTo("UAA.CLIENT.ID_TAKEN");
        assertThat(UaaClient.body(taken).get(FIELD_ERRORS).get(0).get(FIELD).asText()).isEqualTo("clientId");

        String badRedirect = ClientApiSupport.machine("it-api-redirect", X)
                .replace("\"redirectUris\": []", "\"redirectUris\": [\"http://evil.example/cb\"]");
        HttpResponse<String> plainHttp = uaa.bearer(UaaClient.POST, ClientApiSupport.CLIENTS, badRedirect, token);
        assertThat(plainHttp.statusCode()).isEqualTo(400);
        assertThat(UaaClient.body(plainHttp).get(ClientApiSupport.CODE).asText()).isEqualTo("UAA.CLIENT.INVALID_REDIRECT_URI");
        assertThat(UaaClient.body(plainHttp).get("params").get("reason").asText()).isEqualTo("PLAIN_HTTP");

        String longTtl = ClientApiSupport.machine("it-api-ttl", X).replace("PT15M", "PT48H");
        HttpResponse<String> overCeiling = uaa.bearer(UaaClient.POST, ClientApiSupport.CLIENTS, longTtl, token);
        assertThat(overCeiling.statusCode()).isEqualTo(400);
        assertThat(UaaClient.body(overCeiling).get(ClientApiSupport.CODE).asText()).isEqualTo("UAA.CLIENT.TOKEN_TTL_EXCEEDS_POLICY");
        assertThat(UaaClient.body(overCeiling).get(FIELD_ERRORS).get(0).get(FIELD).asText())
                .isEqualTo("tokenSettings.accessTokenTimeToLive");
    }

    @Test
    void theGateHolds() throws IOException, InterruptedException {
        String storeCore = uaa.storeCoreToken();

        assertThat(uaa.bearer(UaaClient.GET, ClientApiSupport.CLIENTS + STATS, null, storeCore).statusCode()).isEqualTo(403);
        assertThat(uaa.bearer(UaaClient.POST, ClientApiSupport.path("rotate-all"), null, storeCore).statusCode())
                .isEqualTo(403);
        assertThat(uaa.anonymous(UaaClient.GET, ClientApiSupport.CLIENTS + STATS).statusCode()).isEqualTo(401);
    }

}
