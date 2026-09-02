package com.asrevo.cvhome.uaa.oauth;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.uaa.support.UaaClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Issued tokens are rows, so they survive a restart and can be revoked.
 *
 * <p>
 * Before the {@code JdbcOAuth2AuthorizationService} bean existed, every token lived in memory: the table was empty
 * whatever happened, and "revoke" had nothing to act on. This is the case that keeps the bean in place.
 * </p>
 */
@DatabaseIntegrationTest
class JdbcAuthorizationPersistenceIntegrationTest {

    private static final String COUNT = "select count(*) from uaa.oauth2_authorization where principal_name = ?";

    private static final String INTROSPECT = "/oauth2/introspect";

    private static final String TOKEN = "token";

    private static final String ACTIVE = "active";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void aTokenIsStoredAndRevocationRemovesIt() throws IOException, InterruptedException {
        UaaClient uaa = new UaaClient(port);
        long before = count();

        String token = uaa.storeCoreToken();
        assertThat(count()).as("rows %s", jdbc.queryForList(
                "select id, authorization_grant_type, access_token_issued_at from uaa.oauth2_authorization")).isEqualTo(before + 1);

        HttpResponse<String> introspected = post(uaa, INTROSPECT, token);
        assertThat(UaaClient.body(introspected).get(ACTIVE).asBoolean()).isTrue();

        HttpResponse<String> revoked = post(uaa, "/oauth2/revoke", token);
        assertThat(revoked.statusCode()).isEqualTo(200);
        // Revocation invalidates the token inside its row rather than deleting the row: the record of the grant
        // stays, and introspection is what tells a resource server the token is dead.
        assertThat(count()).isEqualTo(before + 1);
        introspected = post(uaa, INTROSPECT, token);
        assertThat(UaaClient.body(introspected).get(ACTIVE).asBoolean()).isFalse();
    }

    private long count() {
        Long n = jdbc.queryForObject(COUNT, Long.class, UaaClient.STORE_CORE);
        return n == null ? 0 : n;
    }

    private static HttpResponse<String> post(UaaClient uaa, String path, String token)
            throws IOException, InterruptedException {
        return uaa.clientPost(UaaClient.STORE_CORE, UaaClient.LCL_SECRET, path, Map.of(TOKEN, token));
    }

}
