package com.asrevo.cvhome.uaa.security;

import java.io.IOException;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.uaa.support.UaaClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Five wrong passwords lock the account; the right one is then refused too; an administrator unlocks it.
 */
@DatabaseIntegrationTest
class LockoutIntegrationTest {

    /** A seeded account no other test signs in as. */
    private static final String VICTIM = "org2-store2-moderator";

    private static final String VICTIM_ID = "97022cd5-cc0a-467a-a99a-460b8e2745c3";

    private static final String WRONG = "not-the-password";

    private static final String LOCKED = "/login?error=locked";

    private static final String AUDIT = "select count(*) from uaa.audit_events where event_type = ? and target_name = ?";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbc;

    private long audited(String type) {
        Long n = jdbc.queryForObject(AUDIT, Long.class, type, VICTIM);
        return n == null ? 0 : n;
    }

    @Test
    void fiveFailuresLockThenUnlockRestores() throws IOException, InterruptedException {
        UaaClient uaa = new UaaClient(port);

        for (int i = 1; i <= 4; i++) {
            HttpResponse<String> refused = uaa.login(VICTIM, WRONG);
            assertThat(UaaClient.location(refused)).as("attempt %d", i).endsWith(String.format("/login?error&attemptsLeft=%d", 5 - i));
        }
        HttpResponse<String> fifth = uaa.login(VICTIM, WRONG);
        assertThat(UaaClient.location(fifth)).endsWith(LOCKED);
        assertThat(audited("user.login.failed")).isEqualTo(5);
        assertThat(audited("user.locked")).isEqualTo(1);

        // The right password while locked: refused before the password is even compared.
        HttpResponse<String> locked = uaa.login(VICTIM, UaaClient.PASSWORD);
        assertThat(UaaClient.location(locked)).endsWith(LOCKED);
        assertThat(uaa.session(UaaClient.GET, UaaClient.ME, null).statusCode()).isEqualTo(401);

        String admin = uaa.superAdminToken();
        HttpResponse<String> listed = uaa.bearer(UaaClient.GET, String.format("/api/v1/admin/users/%s", VICTIM_ID), null, admin);
        assertThat(UaaClient.body(listed).get("status").asText()).isEqualTo("LOCKED");
        assertThat(uaa.bearer(UaaClient.POST, String.format("/api/v1/admin/users/%s/unlock", VICTIM_ID), null, admin)
                .statusCode()).isEqualTo(200);
        assertThat(audited("user.unlocked")).isEqualTo(1);

        uaa.clearCookies();
        HttpResponse<String> welcome = uaa.login(VICTIM, UaaClient.PASSWORD);
        assertThat(UaaClient.location(welcome)).endsWith("/");
        assertThat(audited("user.login")).isEqualTo(1);
        HttpResponse<String> me = uaa.session(UaaClient.GET, UaaClient.ME, null);
        assertThat(me.statusCode()).isEqualTo(200);
    }

}
