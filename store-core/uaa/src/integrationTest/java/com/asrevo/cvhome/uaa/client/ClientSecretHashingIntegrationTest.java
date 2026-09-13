package com.asrevo.cvhome.uaa.client;

import java.io.IOException;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.uaa.support.UaaClient;
import com.fasterxml.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * How a client secret is stored, end to end: a salted SHA-256 rather than bcrypt, and a bcrypt hash written before that
 * change still authenticates and is rewritten the first time it does. The seeded clients end up the same way.
 */
@DatabaseIntegrationTest
class ClientSecretHashingIntegrationTest {

    private static final String STORED = "select client_secret from oauth2_registered_client where client_id = ?";

    private static final String OVERWRITE = "update oauth2_registered_client set client_secret = ? where client_id = ?";

    private static final String SHA256_PREFIX = "{sha256}";

    private static final String CLIENT_SECRET = "clientSecret";

    private static final String FRESH = "it-sha256";

    private static final String LEGACY = "it-legacy-bcrypt";

    private static final String WRONG = "not-the-secret-not-the-secret-000";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbc;

    private UaaClient uaa;

    @BeforeEach
    void setUp() {
        uaa = new UaaClient(port);
    }

    private String stored(String clientId) {
        return jdbc.queryForObject(STORED, String.class, clientId);
    }

    @Test
    void aNewClientSecretIsStoredAsSaltedSha256AndAuthenticates() throws IOException, InterruptedException {
        JsonNode created = ClientApiSupport.register(uaa, FRESH);
        String secret = created.get(CLIENT_SECRET).asText();

        assertThat(stored(FRESH)).startsWith(SHA256_PREFIX).doesNotContain(secret);
        assertThat(ClientApiSupport.tokenStatus(uaa, FRESH, secret)).isEqualTo(200);
        assertThat(ClientApiSupport.tokenStatus(uaa, FRESH, WRONG)).isEqualTo(401);
    }

    @Test
    void aBcryptHashFromBeforeAuthenticatesAndIsRewrittenAsSha256OnFirstUse() throws IOException, InterruptedException {
        JsonNode created = ClientApiSupport.register(uaa, LEGACY);
        String secret = created.get(CLIENT_SECRET).asText();
        // What the password encoder wrote for a client secret until now.
        jdbc.update(OVERWRITE, String.format("{bcrypt}%s", new BCryptPasswordEncoder(10).encode(secret)), LEGACY);

        assertThat(ClientApiSupport.tokenStatus(uaa, LEGACY, WRONG)).as("a wrong secret is still refused")
                .isEqualTo(401);
        assertThat(stored(LEGACY)).as("a refused attempt rewrites nothing").startsWith("{bcrypt}");

        assertThat(ClientApiSupport.tokenStatus(uaa, LEGACY, secret)).isEqualTo(200);
        assertThat(stored(LEGACY)).startsWith(SHA256_PREFIX);
        assertThat(ClientApiSupport.tokenStatus(uaa, LEGACY, secret)).as("and it keeps working").isEqualTo(200);
    }

    @Test
    void thereIsNoWayToSetAsecretAPersonChose() throws IOException, InterruptedException {
        JsonNode created = ClientApiSupport.register(uaa, "it-no-reset");
        String id = created.get(ClientApiSupport.CLIENT).get("id").asText();

        // The fast hash is safe only for secrets nobody chose; the operator-chosen reset endpoint is gone.
        HttpResponse<String> reset = uaa.bearer(UaaClient.POST, ClientApiSupport.path(id, "/reset-secret"),
                "{\"newSecret\": \"http-demo-secret\"}", uaa.superAdminToken());

        assertThat(reset.statusCode()).isEqualTo(404);
    }

    @Test
    void theSeededSdkClientIsSha256OnceItHasAuthenticated() throws IOException, InterruptedException {
        // The initializer rewrites configured secrets on boot, and a SQL-seeded bcrypt row is rewritten on first use:
        // either way, a seeded client that has authenticated holds a SHA-256.
        uaa.superAdminToken();

        assertThat(stored(UaaClient.ADMIN_SDK)).startsWith(SHA256_PREFIX);
    }

}
