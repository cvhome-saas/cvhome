package com.asrevo.cvhome.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.endpoint.OAuth2PasswordGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.security.oauth2.client.endpoint.WebClientReactivePasswordTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveRefreshTokenTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
public class ServerCallBearerExchangeFilterFunction implements ExchangeFilterFunction {
    private final WebClientReactivePasswordTokenResponseClient tokenClient;
    private final WebClientReactiveRefreshTokenTokenResponseClient refreshTokenClient;
    private final ReactiveClientRegistrationRepository registrationRepository;
    private final String registrationId;
    private final String username;
    private final String password;
    private OAuth2AccessTokenResponse accessToken;
    private ClientRegistration registration;
    public ServerCallBearerExchangeFilterFunction(WebClientReactivePasswordTokenResponseClient tokenClient,
                                                  WebClientReactiveRefreshTokenTokenResponseClient refreshTokenClient,
                                                  ReactiveClientRegistrationRepository registrationRepository, String registrationId, String username, String password) {
        this.tokenClient = tokenClient;
        this.registrationRepository = registrationRepository;
        this.registrationId = registrationId;
        this.username = username;
        this.password = password;
        this.refreshTokenClient = refreshTokenClient;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        if (accessToken != null) {
            Instant expiresAt = accessToken.getAccessToken().getExpiresAt();
            if (expiresAt == null || !expiresAt.isBefore(Instant.now())) {
                return next.exchange(bearer(request));
            } else if (this.accessToken.getRefreshToken() != null) {
                log.info("will re generate access token using refresh token");
                return this.generateNewAccessToken().flatMap(accessToken -> {
                    this.accessToken = accessToken;
                    return next.exchange(bearer(request));
                });
            } else {
                log.error("token expired and refresh token not exist");
                return next.exchange(bearer(request));
            }
        } else {
            log.info("wil generate access token");
            return getClientRegistration().flatMap(registration -> {
                this.registration = registration;
                return generateAccessToken(registration);
            }).flatMap(accessToken -> {
                this.accessToken = accessToken;
                return next.exchange(bearer(request));
            });
        }
    }

    Mono<ClientRegistration> getClientRegistration() {
        return this.registrationRepository.findByRegistrationId(this.registrationId).switchIfEmpty(Mono.error(() -> new Exception("ClientRegistration not found")));
    }

    Mono<OAuth2AccessTokenResponse> generateAccessToken(ClientRegistration registration) {
        log.info("will generate access token using password Grant type");
        return tokenClient.getTokenResponse(new OAuth2PasswordGrantRequest(registration, username, password));
    }

    Mono<OAuth2AccessTokenResponse> generateNewAccessToken() {
        log.info("will generate access token using refresh Grant type");
        return refreshTokenClient.getTokenResponse(new OAuth2RefreshTokenGrantRequest(this.registration, this.accessToken.getAccessToken(), this.accessToken.getRefreshToken()));
    }

    private ClientRequest bearer(ClientRequest request) {
        return ClientRequest.from(request).headers((headers) -> headers.setBearerAuth(accessToken.getAccessToken().getTokenValue())).build();
    }
}
