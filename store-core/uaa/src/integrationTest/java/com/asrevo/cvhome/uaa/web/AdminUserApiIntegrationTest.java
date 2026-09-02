package com.asrevo.cvhome.uaa.web;

import java.io.IOException;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.uaa.support.UaaClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The permission gate on {@code /api/v1/admin/users}, proven with the tokens uaa itself issues.
 *
 * <p>
 * The gate is double — the filter chain and every method — and the gateway relays an operator's token unchanged,
 * so this is the only thing keeping a {@code store_core} service or an org admin out of platform-wide user
 * administration.
 * </p>
 */
@DatabaseIntegrationTest
class AdminUserApiIntegrationTest {

    private static final String USERS = "/api/v1/admin/users?page=0&count=20";

    @LocalServerPort
    private int port;

    private UaaClient uaa;

    @BeforeEach
    void setUp() {
        uaa = new UaaClient(port);
    }

    @Test
    void superAdminScopeListsUsers() throws IOException, InterruptedException {
        HttpResponse<String> response = uaa.bearer(UaaClient.GET, USERS, null, uaa.superAdminToken());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(UaaClient.body(response).get("content").isArray()).isTrue();
        assertThat(response.body()).doesNotContain("passwordHash").doesNotContain("$2a$");
    }

    @Test
    void storeCoreScopeIsForbidden() throws IOException, InterruptedException {
        String token = uaa.storeCoreToken();
        HttpResponse<String> response = uaa.bearer(UaaClient.GET, USERS, null, token);

        assertThat(response.statusCode()).as("claims %s body %s", UaaClient.claims(token), response.body()).isEqualTo(403);
    }

    @Test
    void anonymousIsUnauthenticated() throws IOException, InterruptedException {
        HttpResponse<String> response = uaa.anonymous(UaaClient.GET, USERS);

        assertThat(response.statusCode()).isEqualTo(401);
    }

}
