package com.asrevo.cvhome.gateway.config;

import com.asrevo.cvhome.s2s.oauth2.PasswordTokenResponseClient;
import com.asrevo.cvhome.s2s.oauth2.RefreshTokenTokenResponseClient;
import com.asrevo.cvhome.s2s.oauth2.ServerCallBearerExchangeFilterFunction;
import com.asrevo.cvhome.s2s.oauth2.ServerCallBearerExchangeInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesMapper;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.endpoint.WebClientReactivePasswordTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveRefreshTokenTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class WebClientServicesConfig {
// @TODO CHECK   DefaultClientCredentialsTokenResponseClient

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
    public PasswordTokenResponseClient passwordTokenResponseClient() {
        return new PasswordTokenResponseClient();
    }

    @Bean
    public RefreshTokenTokenResponseClient refreshTokenTokenResponseClient() {
        return new RefreshTokenTokenResponseClient();
    }

    ClientRegistrationRepository getClientRegistrationRepository(OAuth2ClientProperties properties) {
        List<ClientRegistration> registrations = new ArrayList<>(
                new OAuth2ClientPropertiesMapper(properties).asClientRegistrations().values());
        return new InMemoryClientRegistrationRepository(registrations);
    }

    @Bean
    public RestTemplate restTemplate(PasswordTokenResponseClient responseClient, RefreshTokenTokenResponseClient refreshTokenTokenResponseClient, OAuth2ClientProperties properties) {
        RestTemplate restTemplate = new RestTemplate();
        ClientRegistrationRepository registrationRepository = getClientRegistrationRepository(properties);
        restTemplate.setInterceptors(List.of(new ServerCallBearerExchangeInterceptor(responseClient, refreshTokenTokenResponseClient, registrationRepository, "microservice", "microservice-gateway", "microservice-gateway")));
        return restTemplate;
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

/*   issue with this i cant disable https validation and the validation fail
    @Bean
    public DefaultReactiveOAuth2UserService defaultReactiveOAuth2UserService(WebClient.Builder defaultMicroServiceBuilder) {
        DefaultReactiveOAuth2UserService defaultReactiveOAuth2UserService = new DefaultReactiveOAuth2UserService();
        defaultReactiveOAuth2UserService.setWebClient(defaultMicroServiceBuilder.build());
        return defaultReactiveOAuth2UserService;
    }
*/

/*   @Bean
    public WebClientReactiveAuthorizationCodeTokenResponseClient webClientReactiveAuthorizationCodeTokenResponseClient(WebClient.Builder defaultBalancedBuilder) {
        WebClientReactiveAuthorizationCodeTokenResponseClient webClientReactiveAuthorizationCodeTokenResponseClient = new WebClientReactiveAuthorizationCodeTokenResponseClient();
        webClientReactiveAuthorizationCodeTokenResponseClient.setWebClient(defaultBalancedBuilder.build());
        return webClientReactiveAuthorizationCodeTokenResponseClient;
    }
*/

}
