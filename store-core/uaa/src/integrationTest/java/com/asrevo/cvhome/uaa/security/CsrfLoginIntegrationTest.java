package com.asrevo.cvhome.uaa.security;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.uaa.support.UaaClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CSRF on the application chain: the sign-in form and every session-authenticated write need the cookie's token.
 */
@DatabaseIntegrationTest
class CsrfLoginIntegrationTest {

    private static final String ROLES = "/api/v1/admin/roles";

    @LocalServerPort
    private int port;

    private UaaClient uaa;

    @BeforeEach
    void setUp() {
        uaa = new UaaClient(port);
    }

    @Test
    void loginPagePlantsTheCookieAndTheFormSignsInWithIt() throws IOException, InterruptedException {
        HttpResponse<String> page = uaa.anonymous(UaaClient.GET, UaaClient.LOGIN);
        assertThat(page.statusCode()).isEqualTo(200);
        assertThat(uaa.csrfToken()).isNotEmpty();

        HttpResponse<String> login = uaa.login(UaaClient.SUPER_ADMIN, UaaClient.PASSWORD);

        assertThat(login.statusCode()).isEqualTo(302);
        assertThat(UaaClient.location(login)).endsWith("/");
    }

    @Test
    void aFormWithoutTheTokenIsSentBackAsExpired() throws IOException, InterruptedException {
        uaa.anonymous(UaaClient.GET, UaaClient.LOGIN);

        HttpResponse<String> login = uaa.postForm(UaaClient.LOGIN,
                Map.of("username", UaaClient.SUPER_ADMIN, "password", UaaClient.PASSWORD));

        assertThat(login.statusCode()).isEqualTo(302);
        assertThat(UaaClient.location(login)).endsWith("/login?error=expired");
    }

    @Test
    void aSessionWriteNeedsTheHeader() throws IOException, InterruptedException {
        uaa.login(UaaClient.SUPER_ADMIN, UaaClient.PASSWORD);
        String body = "{\"name\":\"CSRF_PROBE\"}";

        HttpResponse<String> forged = uaa.sessionWithoutCsrf(UaaClient.POST, ROLES, body);
        assertThat(forged.statusCode()).as("headers %s body %s", forged.headers().map(), forged.body()).isEqualTo(403);

        HttpResponse<String> genuine = uaa.session(UaaClient.POST, ROLES, body);
        assertThat(genuine.statusCode()).isEqualTo(200);
        String id = UaaClient.body(genuine).get("id").asText();
        assertThat(uaa.session(UaaClient.DELETE, String.format("%s/%s", ROLES, id), null).statusCode()).isEqualTo(200);
    }

    @Test
    void logoutEndsTheSession() throws IOException, InterruptedException {
        uaa.login(UaaClient.SUPER_ADMIN, UaaClient.PASSWORD);
        assertThat(uaa.session(UaaClient.GET, UaaClient.ME, null).statusCode()).isEqualTo(200);

        HttpResponse<String> logout = uaa.session(UaaClient.GET, "/logout", null);

        assertThat(logout.statusCode()).isEqualTo(302);
        assertThat(UaaClient.location(logout)).endsWith("/login?logout");
        assertThat(uaa.session(UaaClient.GET, UaaClient.ME, null).statusCode()).isEqualTo(401);
    }

}
