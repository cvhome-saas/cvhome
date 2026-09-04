package com.asrevo.cvhome.uaa.keys;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import com.asrevo.cvhome.sso.keys.KeyRotationService;
import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.testsupport.time.MutableClock;
import com.asrevo.cvhome.testsupport.time.TestClockConfiguration;
import com.asrevo.cvhome.uaa.support.UaaClient;
import com.fasterxml.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A rotation keeps in-flight tokens alive for the retire window and kills them after it; the private half never sits
 * in the row in clear; the JWKS serves both keys while one retires and never a private member.
 */
@DatabaseIntegrationTest
@Import(TestClockConfiguration.class)
class KeyRotationIntegrationTest {

    private static final String KEYS = "/api/v1/admin/keys";

    private static final String ROTATE = "/api/v1/admin/keys/rotate";

    private static final String STATUS = "/api/v1/admin/keys/status";

    private static final String JWKS = "/oauth2/jwks";

    private static final String USERS = "/api/v1/admin/users?count=1";

    private static final String KID = "kid";

    private static final String KEY_LIST = "keys";

    private static final String PRIVATE_EXPONENT = "\"d\"";

    private static final String ENVELOPE_PREFIX = "ENC:";

    @LocalServerPort
    private int port;

    @Autowired
    private MutableClock clock;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private KeyRotationService keys;

    private UaaClient uaa;

    @BeforeEach
    void setUp() {
        uaa = new UaaClient(port);
        clock.reset();
    }

    @Test
    void rotationKeepsInFlightTokensUntilTheWindowCloses() throws IOException, InterruptedException {
        String before = uaa.superAdminToken();
        String kidBefore = UaaClient.body(uaa.bearer(UaaClient.GET, STATUS, null, before)).get("activeKid").asText();
        assertThat(uaa.bearer(UaaClient.GET, USERS, null, before).statusCode()).isEqualTo(200);

        HttpResponse<String> rotated = uaa.bearer(UaaClient.POST, ROTATE, null, before);
        assertThat(rotated.statusCode()).isEqualTo(200);
        String kidAfter = UaaClient.body(rotated).get(KID).asText();
        assertThat(kidAfter).isNotEqualTo(kidBefore);

        assertThat(uaa.bearer(UaaClient.GET, USERS, null, before).statusCode()).as("old token during the window").isEqualTo(200);
        String after = uaa.superAdminToken();
        assertThat(UaaClient.claims(after).get("uid")).isNull();
        assertThat(uaa.bearer(UaaClient.GET, USERS, null, after).statusCode()).isEqualTo(200);

        JsonNode jwks = UaaClient.body(uaa.anonymous(UaaClient.GET, JWKS));
        assertThat(jwks.get(KEY_LIST)).hasSize(2);
        assertThat(jwks.toString()).doesNotContain(PRIVATE_EXPONENT).doesNotContain("\"p\"").doesNotContain("\"q\"");

        JsonNode list = UaaClient.body(uaa.bearer(UaaClient.GET, KEYS, null, after));
        assertThat(list).hasSize(2);
        assertThat(list.toString()).doesNotContain("private").doesNotContain(ENVELOPE_PREFIX);

        String stored = jdbc.queryForObject("select private_jwk_enc from uaa.signing_keys where kid = ?", String.class, kidAfter);
        assertThat(stored).startsWith(ENVELOPE_PREFIX).doesNotContain(PRIVATE_EXPONENT);

        clock.advance(Duration.ofDays(8));
        assertThat(keys.retireDue()).isEqualTo(1);
        assertThat(uaa.bearer(UaaClient.GET, USERS, null, before).statusCode()).as("old token after the window").isEqualTo(401);
        assertThat(UaaClient.body(uaa.anonymous(UaaClient.GET, JWKS)).get(KEY_LIST)).hasSize(1);
    }

    @Test
    void theGateHolds() throws IOException, InterruptedException {
        assertThat(uaa.bearer(UaaClient.POST, ROTATE, null, uaa.storeCoreToken()).statusCode()).isEqualTo(403);
        assertThat(uaa.anonymous(UaaClient.GET, KEYS).statusCode()).isEqualTo(401);
    }

}
