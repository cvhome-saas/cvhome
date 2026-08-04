package com.asrevo.cvhome.uaa.sdk;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import com.asrevo.cvhome.uaa.api.errors.UaaApiUnavailableException;
import com.fasterxml.jackson.annotation.JsonProperty;

import tools.jackson.databind.ObjectMapper;

public class OAuth2TokenManager {

    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private static final String CONTENT_TYPE_X_WWW_FORM_URL_ENCODED = "application/x-www-form-urlencoded";

    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final String ACCEPT_HEADER = "Accept";

    private static final String ERR_TOKEN_REQUEST = "Could not obtain an access token from uaa.";

    private final String tokenEndpoint;

    private final String clientId;

    private final String clientSecret;

    private final HttpClient httpClient;

    private final ObjectMapper objectMapper;

    private TokenResponse currentToken;

    private Instant expiryTime;

    public OAuth2TokenManager(String baseUrl, String clientId, String clientSecret) {
        this(baseUrl, clientId, clientSecret, HttpClient.newBuilder().build());
    }

    /**
     * Overload taking the {@link HttpClient}, so the SDK and its token exchange share one client — and so a test can
     * drive both without a uaa to talk to.
     */
    public OAuth2TokenManager(String baseUrl, String clientId, String clientSecret, HttpClient httpClient) {
        this.tokenEndpoint = String.format("%s/oauth2/token", baseUrl);
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * @throws UaaApiUnavailableException the token could not be obtained, so the request the caller wanted was never
     *                                    attempted — whether that is uaa being down or our own credentials being
     *                                    wrong is an operational question, not one the caller can act on
     */
    public synchronized String getAccessToken() throws UaaApiUnavailableException {
        if (currentToken == null || isExpired()) {
            refreshToken();
        }
        return currentToken.accessToken();
    }

    private boolean isExpired() {
        // Refresh 1 minute before actual expiry to be safe
        return expiryTime == null || Instant.now().isAfter(expiryTime.minusSeconds(60));
    }

    private void refreshToken() throws UaaApiUnavailableException {
        // admin-sdk uses client_credentials
        // V2 says client_secret_post for admin-sdk

        String form = String.format("grant_type=client_credentials&scope=super_admin&client_id=%s&client_secret=%s", clientId,
                clientSecret);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tokenEndpoint))
                .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_X_WWW_FORM_URL_ENCODED)
                .header(ACCEPT_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                // Try Basic Auth as fallback
                String auth = String.format("%s:%s", clientId, clientSecret);
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

                String fallbackForm = "grant_type=client_credentials&scope=super_admin";
                HttpRequest fallbackRequest = HttpRequest.newBuilder()
                        .uri(URI.create(tokenEndpoint))
                        .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_X_WWW_FORM_URL_ENCODED)
                        .header(AUTHORIZATION_HEADER, String.format("Basic %s", encodedAuth))
                        .header(ACCEPT_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                        .POST(HttpRequest.BodyPublishers.ofString(fallbackForm))
                        .build();

                response = httpClient.send(fallbackRequest, HttpResponse.BodyHandlers.ofString());
            }

            if (response.statusCode() != 200) {
                // The body is deliberately not carried into the exception: a failed token exchange can echo back
                // client credentials, and this detail reaches a client response.
                throw UaaApiUnavailableException.tokenRequestFailed(ERR_TOKEN_REQUEST, null);
            }
            currentToken = objectMapper.readValue(response.body(), TokenResponse.class);
            expiryTime = Instant.now().plusSeconds(currentToken.expiresIn());
        } catch (IOException e) {
            throw UaaApiUnavailableException.tokenRequestFailed(ERR_TOKEN_REQUEST, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw UaaApiUnavailableException.tokenRequestFailed(ERR_TOKEN_REQUEST, e);
        }
    }

    private record TokenResponse(@JsonProperty("access_token") String accessToken,
                                 @JsonProperty("token_type") String tokenType, @JsonProperty("expires_in") long expiresIn,
                                 @JsonProperty("scope") String scope) {
    }

}
