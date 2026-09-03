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

/** A disabled client gets {@code invalid_client} at the token endpoint, and is back the moment it is enabled. */
@DatabaseIntegrationTest
class ClientDisableIntegrationTest {

    private static final String CLIENT_ID = "it-disable";

    private static final String ENABLED = "enabled";

    @LocalServerPort
    private int port;

    private UaaClient uaa;

    @BeforeEach
    void setUp() {
        uaa = new UaaClient(port);
    }

    @Test
    void disableRefusesTokensAndEnableRestoresThem() throws IOException, InterruptedException {
        String token = uaa.superAdminToken();
        JsonNode created = ClientApiSupport.register(uaa, CLIENT_ID);
        String id = created.get(ClientApiSupport.CLIENT).get("id").asText();
        String secret = created.get("clientSecret").asText();
        assertThat(ClientApiSupport.tokenStatus(uaa, CLIENT_ID, secret)).isEqualTo(200);

        HttpResponse<String> disabled = uaa.bearer(UaaClient.POST, ClientApiSupport.path(id, "/disable"), null, token);
        assertThat(disabled.statusCode()).isEqualTo(200);
        assertThat(UaaClient.body(disabled).get(ClientApiSupport.STATUS).get(ENABLED).asBoolean()).isFalse();
        assertThat(UaaClient.body(disabled).get(ClientApiSupport.STATUS).get("disabledBy").asText()).isEqualTo(UaaClient.ADMIN_SDK);
        assertThat(ClientApiSupport.tokenStatus(uaa, CLIENT_ID, secret)).as("disabled").isEqualTo(401);

        HttpResponse<String> stillReadable = uaa.bearer(UaaClient.GET, ClientApiSupport.path(id), null, token);
        assertThat(stillReadable.statusCode()).as("an operator can still read and re-enable it").isEqualTo(200);

        HttpResponse<String> enabled = uaa.bearer(UaaClient.POST, ClientApiSupport.path(id, "/enable"), null, token);
        assertThat(UaaClient.body(enabled).get(ClientApiSupport.STATUS).get(ENABLED).asBoolean()).isTrue();
        assertThat(ClientApiSupport.tokenStatus(uaa, CLIENT_ID, secret)).as("enabled again").isEqualTo(200);
    }

}
