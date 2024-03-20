package com.asrevo.cvhome.manager.config;

import com.asrevo.cvhome.s2s.oauth2.ServerCallBearerExchangeFilterFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.endpoint.WebClientReactivePasswordTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveRefreshTokenTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class WebClientServicesConfig {

    @Bean("defaultBuilder")
    public WebClient.Builder defaultBuilder() {
        return WebClient.builder();
    }

    @Bean("defaultBalancedBuilder")
    @LoadBalanced
    public WebClient.Builder defaultBalancedBuilder() {
        return WebClient.builder();
    }


    @Bean("defaultWebMicroServiceBuilder")
    public WebClient.Builder defaultWebMicroServiceBuilder(WebClientReactivePasswordTokenResponseClient tokenClient, WebClientReactiveRefreshTokenTokenResponseClient refreshTokenClient, ReactiveClientRegistrationRepository repository) {
        ServerCallBearerExchangeFilterFunction filter = new ServerCallBearerExchangeFilterFunction(tokenClient, refreshTokenClient, repository, "microservice", "microservice-gateway", "microservice-gateway");
        return WebClient.builder().filter(filter);
    }


    @Bean("defaultMicroServiceBuilder")
    @LoadBalanced
    public WebClient.Builder defaultMicroServiceBuilder(WebClientReactivePasswordTokenResponseClient tokenClient, WebClientReactiveRefreshTokenTokenResponseClient refreshTokenClient, ReactiveClientRegistrationRepository repository) {
        ServerCallBearerExchangeFilterFunction filter = new ServerCallBearerExchangeFilterFunction(tokenClient, refreshTokenClient, repository, "microservice", "microservice-gateway", "microservice-gateway");
        return WebClient.builder().filter(filter);
    }

    @Bean
    public WebClientReactivePasswordTokenResponseClient reactivePasswordTokenResponseClient(@Qualifier("defaultBuilder") WebClient.Builder defaultBuilder) {
        WebClientReactivePasswordTokenResponseClient client = new WebClientReactivePasswordTokenResponseClient();
        client.setWebClient(defaultBuilder.build());
        return client;
    }

    @Bean
    public WebClientReactiveRefreshTokenTokenResponseClient reactiveRefreshTokenTokenResponseClient(@Qualifier("defaultBuilder") WebClient.Builder defaultBuilder) {
        WebClientReactiveRefreshTokenTokenResponseClient refreshTokenClient = new WebClientReactiveRefreshTokenTokenResponseClient();
        refreshTokenClient.setWebClient(defaultBuilder.build());
        return refreshTokenClient;
    }

}
