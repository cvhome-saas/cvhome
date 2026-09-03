package com.asrevo.cvhome.uaa.oauth;

import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.util.UriComponentsBuilder;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.uaa.support.UaaClient;
import com.fasterxml.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The console's login, end to end on uaa alone: form login, {@code /oauth2/authorize} with PKCE, consent, the code,
 * the token — and what the token says.
 *
 * <p>
 * Also the canary for the seed: {@code web-app} now requires PKCE and issues short-lived tokens, and this walk is
 * what proves a code exchange still works under those settings.
 * </p>
 */
@DatabaseIntegrationTest
class LoginFlowIntegrationTest {

    /** One of the redirect URIs the boot-time initializer registers for web-app from common-config.yml. */
    private static final String REDIRECT = "http://gateway.com:8000/login/oauth2/code/uaa";

    private static final String VERIFIER = "0123456789abcdef0123456789abcdef0123456789abcdef";

    private static final String AUTHORIZE = "/oauth2/authorize";

    private static final String CLIENT_ID = "client_id";

    private static final String SCOPE = "scope";

    private static final String OPENID = "openid";

    private static final String STATE = "state";

    private static final String CODE = "code";

    private static final String REDIRECT_URI = "redirect_uri";

    private static final String STATE_1 = "s1";

    private static final String VERIFIER_PARAM = "code_verifier";

    @LocalServerPort
    private int port;

    private static String authorizeUri(String state, String challenge) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(AUTHORIZE)
                .queryParam("response_type", CODE).queryParam(CLIENT_ID, UaaClient.WEB_APP)
                .queryParam(REDIRECT_URI, REDIRECT).queryParam(SCOPE, OPENID).queryParam(STATE, state);
        if (challenge != null) {
            builder.queryParam("code_challenge", challenge).queryParam("code_challenge_method", "S256");
        }
        return builder.build().toUriString();
    }

    @Test
    void authorizationCodeWithPkceYieldsATokenWithTheRightClaims() throws Exception {
        UaaClient uaa = new UaaClient(port);
        uaa.login(UaaClient.ORG1_STORE1_ADMIN, UaaClient.PASSWORD);

        // web-app requires consent, but `openid` alone never asks for it, so the code comes straight back — which
        // is exactly what the gateway's login sees.
        HttpResponse<String> granted = uaa.session(UaaClient.GET, authorizeUri(STATE_1, challenge(VERIFIER)), null);
        assertThat(granted.statusCode()).as("headers %s", granted.headers().map()).isEqualTo(302);
        String location = UaaClient.location(granted);
        assertThat(location).startsWith(REDIRECT);
        String code = UriComponentsBuilder.fromUriString(location).build().getQueryParams().getFirst(CODE);
        assertThat(code).isNotBlank();

        HttpResponse<String> tokens = uaa.clientPost(UaaClient.WEB_APP, UaaClient.LCL_SECRET, "/oauth2/token",
                Map.of("grant_type", "authorization_code", CODE, code, REDIRECT_URI, REDIRECT, VERIFIER_PARAM, VERIFIER));
        assertThat(tokens.statusCode()).isEqualTo(200);
        JsonNode body = UaaClient.body(tokens);
        assertThat(body.get("expires_in").asInt()).isLessThanOrEqualTo(900);
        JsonNode claims = UaaClient.claims(body.get("access_token").asText());
        assertThat(claims.get("sub").asText()).isEqualTo(UaaClient.ORG1_STORE1_ADMIN);
        assertThat(claims.get("uid").asText()).isEqualTo("60ab49a5-7f06-4b5a-be81-9b30bb6559ae");
        assertThat(claims.get("org").asText()).isEqualTo("21f023932bc66470c104b76f");
        assertThat(claims.get("store").asText()).isEqualTo("65f023632bc46470c104b76f");
        assertThat(claims.get("roles").toString()).contains("STORE_ADMIN");
        assertThat(claims.get("permissions").toString()).contains("users:read").contains("users:write");
        assertThat(claims.get("iss").asText()).isEqualTo(IssuerPinningIntegrationTest.PINNED);
        JsonNode idToken = UaaClient.claims(body.get("id_token").asText());
        assertThat(idToken.get("email").asText()).isEqualTo("org1-store1-admin@mail.com");
        assertThat(idToken.get("given_name").asText()).isEqualTo("Store1");
    }

    @Test
    void aCodeWithoutAVerifierIsRefused() throws Exception {
        UaaClient uaa = new UaaClient(port);
        uaa.login(UaaClient.ORG1_ADMIN, UaaClient.PASSWORD);

        HttpResponse<String> response = uaa.session(UaaClient.GET, authorizeUri("s2", null), null);

        // PKCE is required for web-app: an authorization request without a challenge never reaches consent.
        assertThat(response.statusCode()).isIn(302, 400);
        if (response.statusCode() == 302) {
            assertThat(UaaClient.location(response)).contains("error=invalid_request");
        }
    }

    private static String challenge(String verifier) throws NoSuchAlgorithmException {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }

}
