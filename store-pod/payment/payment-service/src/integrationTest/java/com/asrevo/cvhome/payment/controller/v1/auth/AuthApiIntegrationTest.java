package com.asrevo.cvhome.payment.controller.v1.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import com.asrevo.cvhome.payment.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;
import com.asrevo.cvhome.testsupport.http.ApiClient;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The token introspection endpoints the console calls to find out who it is talking as.
 *
 * <p>
 * They live outside {@code /api/*&#47;private/**}, so the filter chain lets an anonymous caller through and the
 * controller is the only thing standing between a missing token and a response — worth pinning, because a browser
 * with an expired session hits exactly this path.
 * </p>
 */
@StorageIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class AuthApiIntegrationTest {

    private static final String CURRENT = "/api/v1/auth/current";

    private static final String ME = "/api/v1/auth/me";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    private ApiClient api;

    private String token;

    @BeforeEach
    void setUp() {
        api = new ApiClient(port);
        token = new Tokens(signer).staff(Tokens.ROLE_STORE_ADMIN, Tokens.STORE_1);
    }

    @Test
    void currentAnswersTheClaimsOfTheBearerToken() {
        var response = api.get(CURRENT, token);

        ApiClient.expect(response, HttpStatus.OK);
        assertThat(ApiClient.json(response).toString()).contains(Tokens.STORE_1, Tokens.ROLE_STORE_ADMIN);
    }

    @Test
    void meAnswersTheAuthenticationHeldInTheSecurityContext() {
        var response = api.get(ME, token);

        ApiClient.expect(response, HttpStatus.OK);
        assertThat(ApiClient.json(response).toString()).contains(Tokens.STORE_1);
    }

}
