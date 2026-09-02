package com.asrevo.cvhome.uaa.client;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;

import com.asrevo.cvhome.uaa.support.UaaClient;
import com.fasterxml.jackson.databind.JsonNode;

/** The client registrations the client tests create, and the token call they make with the answered secret. */
final class ClientApiSupport {

    static final String CLIENTS = "/api/v1/admin/clients";

    static final String TOKEN = "/oauth2/token";

    static final String SCOPE = "store_core";

    static final String CLIENT = "client";

    static final String STATUS = "status";

    static final String CODE = "code";

    private static final String MACHINE = """
            {"clientId": "%s", "clientName": "%s",
             "clientAuthenticationMethods": ["client_secret_basic"], "authorizationGrantTypes": ["client_credentials"],
             "redirectUris": [], "postLogoutRedirectUris": [], "scopes": ["store_core"],
             "clientSettings": {"requireProofKey": false, "requireAuthorizationConsent": false, "customSettings": {}},
             "tokenSettings": {"accessTokenTimeToLive": "PT15M", "reuseRefreshTokens": false,
                               "x509CertificateBoundAccessTokens": false, "customSettings": {}},
             "status": {"description": "%s"}}""";

    private ClientApiSupport() {
    }

    /** {@code /api/v1/admin/clients/{id}} plus an optional sub-path such as {@code /rotate-secret}. */
    static String path(String id, String suffix) {
        return String.format("%s/%s%s", CLIENTS, id, suffix);
    }

    static String path(String id) {
        return path(id, "");
    }

    static String machine(String clientId, String description) {
        return String.format(MACHINE, clientId, clientId, description);
    }

    /** Registers a machine client and answers the creation body: {@code client} and the one-time {@code clientSecret}. */
    static JsonNode register(UaaClient uaa, String clientId) throws IOException, InterruptedException {
        HttpResponse<String> created = uaa.bearer(UaaClient.POST, CLIENTS, machine(clientId, "integration test"),
                uaa.superAdminToken());
        if (created.statusCode() != 201) {
            throw new IllegalStateException(String.format("create answered %d: %s", created.statusCode(), created.body()));
        }
        return UaaClient.body(created);
    }

    /** The token endpoint's status for a client id and secret: 200 when they authenticate, 401 when they do not. */
    static int tokenStatus(UaaClient uaa, String clientId, String secret) throws IOException, InterruptedException {
        return uaa.clientPost(clientId, secret, TOKEN, Map.of("grant_type", "client_credentials", "scope", SCOPE))
                .statusCode();
    }

}
