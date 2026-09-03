package com.asrevo.cvhome.uaa.web;

import java.io.IOException;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.uaa.support.UaaClient;
import com.fasterxml.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code /api/v1/auth/me} describes a person, never a principal object.
 */
@DatabaseIntegrationTest
class MeEndpointIntegrationTest {

    private static final String AUTHORITIES = "authorities";

    @LocalServerPort
    private int port;

    @Test
    void anonymousGetsAProblemNotARedirect() throws IOException, InterruptedException {
        HttpResponse<String> response = new UaaClient(port).anonymous(UaaClient.GET, UaaClient.ME);

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.headers().firstValue(UaaClient.LOCATION)).isEmpty();
    }

    @Test
    void aSessionIsDescribedAsTheAccount() throws IOException, InterruptedException {
        UaaClient uaa = new UaaClient(port);
        uaa.login(UaaClient.ORG1_ADMIN, UaaClient.PASSWORD);

        HttpResponse<String> response = uaa.session(UaaClient.GET, UaaClient.ME, null);

        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode me = UaaClient.body(response);
        assertThat(me.get("username").asText()).isEqualTo(UaaClient.ORG1_ADMIN);
        assertThat(me.get("uid").asText()).isEqualTo("318f2fd5-e235-4c2e-ab7e-6c949ba4cdd4");
        assertThat(me.get("email").asText()).isEqualTo("org1-admin@mail.com");
        assertThat(me.get("authenticatedVia").asText()).isEqualTo("SESSION");
        assertThat(me.get("roles").toString()).contains("ORG_ADMIN");
        assertThat(me.get("permissions").toString()).contains("users:invite");
        assertThat(me.get(AUTHORITIES).toString()).contains("PERM_users:invite");
        assertThat(me.get(AUTHORITIES).toString()).contains("ROLE_ORG_ADMIN");
        assertThat(response.body()).doesNotContain("password").doesNotContain("credentials");
    }

    @Test
    void aServiceClientIsRefusedAsNotAUser() throws IOException, InterruptedException {
        UaaClient uaa = new UaaClient(port);

        HttpResponse<String> response = uaa.bearer(UaaClient.GET, UaaClient.ME, null, uaa.superAdminToken());

        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(UaaClient.body(response).get("code").asText()).isEqualTo("UAA.AUTH.NOT_A_USER_PRINCIPAL");
    }

}
