package com.asrevo.cvhome.gateway.config.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.client.endpoint.OAuth2PasswordGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

import java.io.IOException;
import java.time.Instant;

@Slf4j
public class ServerCallBearerExchangeInterceptor implements ClientHttpRequestInterceptor {
    private final PasswordTokenResponseClient tokenClient;
    private final RefreshTokenTokenResponseClient refreshTokenClient;
    private final String username;
    private final String password;
    private final ClientRegistration registration;
    private OAuth2AccessTokenResponse accessToken;


    public ServerCallBearerExchangeInterceptor(PasswordTokenResponseClient tokenClient, RefreshTokenTokenResponseClient refreshTokenClient, ClientRegistrationRepository registrationRepository, String registrationId, String username, String password) {
        this.tokenClient = tokenClient;
        this.refreshTokenClient = refreshTokenClient;
        this.username = username;
        this.password = password;
        this.registration = registrationRepository.findByRegistrationId(registrationId);
    }


    private ClientHttpResponse bearer(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (this.accessToken != null) {
            request.getHeaders().add(HttpHeaders.AUTHORIZATION,
                    "Bearer " + this.accessToken.getAccessToken().getTokenValue());
        }
        return execution.execute(request, body);
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (accessToken != null) {
            Instant expiresAt = accessToken.getAccessToken().getExpiresAt();
            if (expiresAt == null || !expiresAt.isBefore(Instant.now())) {
                return bearer(request, body, execution);
            } else if (this.accessToken.getRefreshToken() != null) {
                log.info("will re generate access token using refresh token");
                this.accessToken = this.generateNewAccessToken();
                return bearer(request, body, execution);
            } else {
                log.error("token expired and refresh token not exist");
                return bearer(request, body, execution);
            }
        } else {
            log.info("wil generate access token");
            this.accessToken = generateAccessToken(registration);
            return bearer(request, body, execution);
        }
    }


    OAuth2AccessTokenResponse generateAccessToken(ClientRegistration registration) {
        log.info("will generate access token using password Grant type");
        return tokenClient.getTokenResponse(new OAuth2PasswordGrantRequest(registration, username, password));
    }

    OAuth2AccessTokenResponse generateNewAccessToken() {
        log.info("will generate access token using refresh Grant type");
        return refreshTokenClient.getTokenResponse(new OAuth2RefreshTokenGrantRequest(this.registration, this.accessToken.getAccessToken(), this.accessToken.getRefreshToken()));
    }
}
