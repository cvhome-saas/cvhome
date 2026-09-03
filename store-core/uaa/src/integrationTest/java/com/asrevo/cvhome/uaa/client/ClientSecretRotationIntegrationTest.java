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
 * Rotation with a grace window, end to end at the token endpoint: the old secret keeps working until it is revoked,
 * the new one works from the start, and a rotated-out secret revoked early stops at once.
 */
@DatabaseIntegrationTest
class ClientSecretRotationIntegrationTest {

    private static final String CLIENT_ID = "it-rotation";

    private static final String CLIENT_SECRET = "clientSecret";

    private static final String PREVIOUS_UNTIL = "previousSecretUntil";

    private static final String PREVIOUS_SECRET = "/previous-secret";

    @LocalServerPort
    private int port;

    private UaaClient uaa;

    @BeforeEach
    void setUp() {
        uaa = new UaaClient(port);
    }

    @Test
    void oldSecretWorksInGraceAndDiesWhenRevoked() throws IOException, InterruptedException {
        String token = uaa.superAdminToken();
        JsonNode created = ClientApiSupport.register(uaa, CLIENT_ID);
        String id = created.get(ClientApiSupport.CLIENT).get("id").asText();
        String first = created.get(CLIENT_SECRET).asText();
        assertThat(ClientApiSupport.tokenStatus(uaa, CLIENT_ID, first)).isEqualTo(200);

        HttpResponse<String> rotated = uaa.bearer(UaaClient.POST, ClientApiSupport.path(id, "/rotate-secret"), null, token);
        assertThat(rotated.statusCode()).isEqualTo(200);
        String second = UaaClient.body(rotated).get(CLIENT_SECRET).asText();
        assertThat(second).isNotEqualTo(first);
        assertThat(UaaClient.body(rotated).get(PREVIOUS_UNTIL).isNull()).isFalse();

        assertThat(ClientApiSupport.tokenStatus(uaa, CLIENT_ID, first)).as("old secret inside the grace window").isEqualTo(200);
        assertThat(ClientApiSupport.tokenStatus(uaa, CLIENT_ID, second)).as("new secret").isEqualTo(200);
        assertThat(ClientApiSupport.tokenStatus(uaa, CLIENT_ID, "wrong")).isEqualTo(401);

        HttpResponse<String> detail = uaa.bearer(UaaClient.GET, ClientApiSupport.path(id), null, token);
        assertThat(UaaClient.body(detail).get(ClientApiSupport.STATUS).get(PREVIOUS_UNTIL).isNull()).isFalse();

        HttpResponse<String> revoked = uaa.bearer(UaaClient.DELETE, ClientApiSupport.path(id, PREVIOUS_SECRET), null, token);
        assertThat(revoked.statusCode()).isEqualTo(200);
        assertThat(ClientApiSupport.tokenStatus(uaa, CLIENT_ID, first)).as("old secret after revocation").isEqualTo(401);
        assertThat(ClientApiSupport.tokenStatus(uaa, CLIENT_ID, second)).isEqualTo(200);

        HttpResponse<String> nothingLeft = uaa.bearer(UaaClient.DELETE, ClientApiSupport.path(id, PREVIOUS_SECRET), null,
                token);
        assertThat(nothingLeft.statusCode()).isEqualTo(404);
        assertThat(UaaClient.body(nothingLeft).get(ClientApiSupport.CODE).asText()).isEqualTo("UAA.CLIENT.NO_PREVIOUS_SECRET");
    }

}
