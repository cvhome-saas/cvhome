package com.asrevo.cvhome.uaa.web;

import java.io.IOException;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.uaa.support.UaaClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The settings document round-trips, refuses a stale version, and refuses a value out of range.
 */
@DatabaseIntegrationTest
class AdminSettingsApiIntegrationTest {

    private static final String SETTINGS = "/api/v1/admin/settings";

    private static final String VERSION = "version";

    private static final String LOCKOUT = "lockout";

    private static final String THRESHOLD = "threshold";

    private static final String CODE = "code";

    private static final ObjectMapper JSON = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Test
    void roundTripThenStaleThenInvalid() throws IOException, InterruptedException {
        UaaClient uaa = new UaaClient(port);
        String token = uaa.superAdminToken();

        HttpResponse<String> read = uaa.bearer(UaaClient.GET, SETTINGS, null, token);
        assertThat(read.statusCode()).isEqualTo(200);
        ObjectNode doc = (ObjectNode) UaaClient.body(read);
        long version = doc.get(VERSION).asLong();
        ((ObjectNode) doc.get(LOCKOUT)).put(THRESHOLD, 7);

        HttpResponse<String> written = uaa.bearer(UaaClient.PUT, SETTINGS, JSON.writeValueAsString(doc), token);
        assertThat(written.statusCode()).as(written.body()).isEqualTo(200);
        JsonNode after = UaaClient.body(written);
        assertThat(after.get(LOCKOUT).get(THRESHOLD).asInt()).isEqualTo(7);
        assertThat(after.get(VERSION).asLong()).isEqualTo(version + 1);
        assertThat(after.get("updatedBy").asText()).isEqualTo(UaaClient.ADMIN_SDK);

        // The same document again carries the old version.
        HttpResponse<String> stale = uaa.bearer(UaaClient.PUT, SETTINGS, JSON.writeValueAsString(doc), token);
        assertThat(stale.statusCode()).isEqualTo(409);
        assertThat(UaaClient.body(stale).get(CODE).asText()).isEqualTo("UAA.SETTINGS.CONFLICT");

        doc.put(VERSION, version + 1);
        ((ObjectNode) doc.get(LOCKOUT)).put(THRESHOLD, 0);
        HttpResponse<String> invalid = uaa.bearer(UaaClient.PUT, SETTINGS, JSON.writeValueAsString(doc), token);
        assertThat(invalid.statusCode()).isEqualTo(400);
        assertThat(UaaClient.body(invalid).get(CODE).asText()).isEqualTo("UAA.SETTINGS.INVALID");

        assertThat(uaa.bearer(UaaClient.GET, SETTINGS, null, uaa.storeCoreToken()).statusCode()).isEqualTo(403);
    }

}
