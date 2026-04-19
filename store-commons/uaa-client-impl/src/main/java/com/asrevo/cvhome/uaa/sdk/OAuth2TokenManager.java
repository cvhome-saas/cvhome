package com.asrevo.cvhome.uaa.sdk;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import com.asrevo.cvhome.uaa.exception.ApiException;
import com.fasterxml.jackson.annotation.JsonProperty;

import tools.jackson.databind.ObjectMapper;

public class OAuth2TokenManager {

    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private static final String CONTENT_TYPE_X_WWW_FORM_URL_ENCODED = "application/x-www-form-urlencoded";

    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final String ACCEPT_HEADER = "Accept";

    private final String tokenEndpoint;

    private final String clientId;

    private final String clientSecret;

    private final HttpClient httpClient;

    private final ObjectMapper objectMapper;

    private TokenResponse currentToken;

    private Instant expiryTime;

    public OAuth2TokenManager(String baseUrl, String clientId, String clientSecret) {
        this.tokenEndpoint = baseUrl + "/oauth2/token";
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.httpClient = HttpClient.newBuilder().build();
        this.objectMapper = new ObjectMapper();
    }

    public synchronized String getAccessToken() {
        if (currentToken == null || isExpired()) {
            refreshToken();
        }
        return currentToken.accessToken();
    }

    private boolean isExpired() {
        // Refresh 1 minute before actual expiry to be safe
        return expiryTime == null || Instant.now().isAfter(expiryTime.minusSeconds(60));
    }

    private void refreshToken() {
        // admin-sdk uses client_credentials
        // V2 says client_secret_post for admin-sdk

        String form = "grant_type=client_credentials&scope=super_admin&client_id=" + clientId + "&client_secret="
                + clientSecret;

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
                String auth = clientId + ":" + clientSecret;
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

                String fallbackForm = "grant_type=client_credentials&scope=super_admin";
                HttpRequest fallbackRequest = HttpRequest.newBuilder()
                        .uri(URI.create(tokenEndpoint))
                        .header(CONTENT_TYPE_HEADER, CONTENT_TYPE_X_WWW_FORM_URL_ENCODED)
                        .header(AUTHORIZATION_HEADER, "Basic " + encodedAuth)
                        .header(ACCEPT_HEADER, CONTENT_TYPE_APPLICATION_JSON)
                        .POST(HttpRequest.BodyPublishers.ofString(fallbackForm))
                        .build();

                response = httpClient.send(fallbackRequest, HttpResponse.BodyHandlers.ofString());
            }

            if (response.statusCode() != 200) {
                throw new ApiException("Failed to get token: " + response.body());
            }
            currentToken = objectMapper.readValue(response.body(), TokenResponse.class);
            expiryTime = Instant.now().plusSeconds(currentToken.expiresIn());
        } catch (IOException e) {
            throw new ApiException("Error during token request", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Error during token request", e);
        }
    }

    private record TokenResponse(@JsonProperty("access_token") String accessToken,
                                 @JsonProperty("token_type") String tokenType, @JsonProperty("expires_in") long expiresIn,
                                 @JsonProperty("scope") String scope) {
    }

}
