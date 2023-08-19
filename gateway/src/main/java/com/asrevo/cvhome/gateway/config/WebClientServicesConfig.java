package com.asrevo.cvhome.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.endpoint.WebClientReactivePasswordTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveRefreshTokenTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class WebClientServicesConfig {

    @Bean("defaultWebBuilder")
    @LoadBalanced
    public WebClient.Builder defaultWebBuilder(ReactiveClientRegistrationRepository clientRegistrationRepository, ServerOAuth2AuthorizedClientRepository serverOAuth2AuthorizedClientRepository) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrationRepository, serverOAuth2AuthorizedClientRepository/*new UnAuthenticatedServerOAuth2AuthorizedClientRepository()*/);
        oauth.setDefaultClientRegistrationId("keycloak");
        return WebClient.builder().filter(oauth);
    }

    @Bean("defaultMicroServiceBuilder")
    @LoadBalanced
    public WebClient.Builder defaultMicroServiceBuilder(WebClientReactivePasswordTokenResponseClient tokenClient, WebClientReactiveRefreshTokenTokenResponseClient refreshTokenClient, ReactiveClientRegistrationRepository repository) {
        ServerCallBearerExchangeFilterFunction filter = new ServerCallBearerExchangeFilterFunction(tokenClient, refreshTokenClient, repository, "microservice", "microservice-gateway", "microservice-gateway");
        return WebClient.builder().filter(filter);
    }

    @Bean("defaultBuilder")
    public WebClient.Builder defaultBuilder() {
        return WebClient.builder();
    }

    @Bean("defaultBalancedBuilder")
    @LoadBalanced
    public WebClient.Builder defaultBalancedBuilder() {
        return WebClient.builder();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public WebClientReactivePasswordTokenResponseClient passwordTokenResponseClient(@Qualifier("defaultBuilder") WebClient.Builder defaultBuilder) {
        WebClientReactivePasswordTokenResponseClient client = new WebClientReactivePasswordTokenResponseClient();
        client.setWebClient(defaultBuilder.build());
        return client;
    }

    @Bean
    public WebClientReactiveRefreshTokenTokenResponseClient refreshTokenClient(@Qualifier("defaultBuilder") WebClient.Builder defaultBuilder) {
        WebClientReactiveRefreshTokenTokenResponseClient refreshTokenClient = new WebClientReactiveRefreshTokenTokenResponseClient();
        refreshTokenClient.setWebClient(defaultBuilder.build());
        return refreshTokenClient;
    }
}
