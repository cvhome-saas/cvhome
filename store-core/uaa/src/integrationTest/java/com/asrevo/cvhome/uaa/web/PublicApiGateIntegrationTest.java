package com.asrevo.cvhome.uaa.web;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.uaa.support.UaaClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The public chain: anonymous, stateless, answers problems rather than login redirects — and the SPA pages a link
 * lands on are reachable without a session, while everything else still is not.
 */
@DatabaseIntegrationTest
class PublicApiGateIntegrationTest {

    @LocalServerPort
    private int port;

    private UaaClient uaa;

    @BeforeEach
    void setUp() {
        uaa = new UaaClient(port);
    }

    @Test
    void anUnknownTokenIsAProblemNotARedirect() throws Exception {
        HttpResponse<String> missing = uaa.anonymous(UaaClient.GET, "/api/v1/public/invitations/no-such-token");
        assertThat(missing.statusCode()).isEqualTo(404);
        assertThat(missing.body()).contains("UAA.INVITATION.NOT_USABLE");
        assertThat(missing.headers().firstValue("set-cookie")).as("stateless").isEmpty();

        HttpResponse<String> reset = uaa.anonymous(UaaClient.POST, "/api/v1/public/password-resets/no-such-token/accept",
                "{\"password\": \"Whatever-Passw0rd-1\"}");
        assertThat(reset.statusCode()).isEqualTo(404);
        assertThat(reset.body()).contains("UAA.PASSWORD.RESET_TOKEN_NOT_USABLE");
    }

    @Test
    void theAcceptPagesAreOpenAndTheConsoleIsNot() throws Exception {
        assertThat(uaa.anonymous(UaaClient.GET, "/accept-invitation?token=x").statusCode()).isEqualTo(200);
        assertThat(uaa.anonymous(UaaClient.GET, "/reset-password?token=x").statusCode()).isEqualTo(200);
        // A JSON-accepting anonymous call is refused outright; a browser would be redirected to /login instead.
        HttpResponse<String> console = uaa.anonymous(UaaClient.GET, "/users");
        assertThat(console.statusCode()).isIn(302, 401);
        assertThat(console.body()).doesNotContain("<app-root");
    }

}
