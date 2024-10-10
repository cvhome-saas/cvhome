package com.asrevo.cvhome.s2s.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveClientCredentialsTokenResponseClient;
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
    private final WebClientReactiveClientCredentialsTokenResponseClient tokenClient;
    private final ReactiveClientRegistrationRepository registrationRepository;
    private final String registrationId;
    private OAuth2AccessTokenResponse accessToken;

    public ServerCallBearerExchangeFilterFunction(WebClientReactiveClientCredentialsTokenResponseClient tokenClient,
                                                  ReactiveClientRegistrationRepository registrationRepository, String registrationId) {
        this.tokenClient = tokenClient;
        this.registrationRepository = registrationRepository;
        this.registrationId = registrationId;

    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        if (accessToken != null) {
            Instant expiresAt = accessToken.getAccessToken().getExpiresAt();
            if (expiresAt == null || !expiresAt.isBefore(Instant.now())) {
                return next.exchange(bearer(request));
            } else {
                log.info("wil generate access token because expired");
                return getNewAccessToken()
                        .flatMap(it -> next.exchange(bearer(request)));
            }
        } else {
            log.info("wil generate access token for first time");
            return getNewAccessToken()
                    .flatMap(it -> next.exchange(bearer(request)));
        }
    }

    private Mono<OAuth2AccessTokenResponse> getNewAccessToken() {
        return getClientRegistration().flatMap(this::doGenerateAccessToken)
                .map(accessToken -> {
                    this.accessToken = accessToken;
                    return accessToken;
                });
    }

    Mono<ClientRegistration> getClientRegistration() {
        return this.registrationRepository.findByRegistrationId(this.registrationId).switchIfEmpty(Mono.error(() -> new Exception("ClientRegistration not found")));
    }

    Mono<OAuth2AccessTokenResponse> doGenerateAccessToken(ClientRegistration registration) {
        log.info("will generate access token using password Grant type");
        return tokenClient.getTokenResponse(new OAuth2ClientCredentialsGrantRequest(registration));
    }

    private ClientRequest bearer(ClientRequest request) {
        return ClientRequest.from(request).headers((headers) -> headers.setBearerAuth(accessToken.getAccessToken().getTokenValue())).build();
    }
}
