package com.asrevo.cvhome.s2s.config.internal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;

import lombok.extern.slf4j.Slf4j;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.REACTIVE;
import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

@Configuration
@Slf4j
@Import({WebClientServicesConfig.ReactiveWebClientServicesConfig.class,
        WebClientServicesConfig.ServletWebClientServicesConfig.class})
@SuppressWarnings("java:S1118")
public class WebClientServicesConfig {
    private static final String S2S_CLIENT_REGISTRATION_ID = "s2s";

    @Configuration
    @ConditionalOnWebApplication(type = REACTIVE)
    @ConditionalOnClass(OAuth2AuthorizedClientManager.class)
    static class ReactiveWebClientServicesConfig {

        @Bean
        public AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                ReactiveClientRegistrationRepository clientRegistrationRepository,
                ReactiveOAuth2AuthorizedClientService authorizedClientService) {

            AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager manager =
                    new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                            clientRegistrationRepository, authorizedClientService);
            manager.setAuthorizedClientProvider(
                    ReactiveOAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().refreshToken().build());

            return manager;
        }

        @Bean("microServiceWebClientBuilder")
        @LoadBalanced
        public WebClient.Builder microServiceWebClientBuilder(
                AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
            ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Filter = new ServerOAuth2AuthorizedClientExchangeFilterFunction(
                    authorizedClientManager);
            oauth2Filter.setDefaultClientRegistrationId(S2S_CLIENT_REGISTRATION_ID);

            return WebClient.builder().filter(oauth2Filter);
        }

        @Bean("microServiceWebClient")
        public WebClient microServiceWebClient(
                @Qualifier("microServiceWebClientBuilder") WebClient.Builder microServiceWebClientBuilder) {
            return microServiceWebClientBuilder.build();
        }

        @Bean("defaultWebClientBuilder")
        public WebClient.Builder defaultWebClientBuilder(
                AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
            ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Filter = new ServerOAuth2AuthorizedClientExchangeFilterFunction(
                    authorizedClientManager);
            oauth2Filter.setDefaultClientRegistrationId(S2S_CLIENT_REGISTRATION_ID);

            return WebClient.builder().filter(oauth2Filter);
        }

        @Bean("defaultWebClient")
        public WebClient defaultWebClient(
                @Qualifier("defaultWebClientBuilder") WebClient.Builder defaultWebClientBuilder) {
            return defaultWebClientBuilder.build();
        }

        @Bean
        public WebClientBuilder webClientBuilder(Environment environment,
                                                 @Qualifier("microServiceWebClient") WebClient microServiceWebClient,
                                                 ServiceDomainProperties serviceDomainProperties) {
            return new WebClientBuilder(environment, microServiceWebClient.mutate(), serviceDomainProperties);
        }

    }

    @Configuration
    @ConditionalOnWebApplication(type = SERVLET)
    @ConditionalOnClass(OAuth2AuthorizedClientManager.class)
    static class ServletWebClientServicesConfig {

        @Bean
        public AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientServiceOAuth2AuthorizedClientManager(
                ClientRegistrationRepository clientRegistrationRepository,
                OAuth2AuthorizedClientService authorizedClientService) {

            AuthorizedClientServiceOAuth2AuthorizedClientManager manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                    clientRegistrationRepository, authorizedClientService);
            manager.setAuthorizedClientProvider(
                    OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().refreshToken().build());

            return manager;
        }

        @Bean("defaultRestClientBuilder")
        public RestClient.Builder defaultRestClientBuilder(
                AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager) {

            OAuth2ClientHttpRequestInterceptor requestInterceptor = new OAuth2ClientHttpRequestInterceptor(
                    authorizedClientManager);

            requestInterceptor.setClientRegistrationIdResolver(clientRequest -> S2S_CLIENT_REGISTRATION_ID);

            return RestClient.builder().requestInterceptor(requestInterceptor);
        }

        @Bean("microServiceRestClientBuilder")
        @LoadBalanced
        public RestClient.Builder microServiceRestClientBuilder(
                AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager) {

            OAuth2ClientHttpRequestInterceptor requestInterceptor = new OAuth2ClientHttpRequestInterceptor(
                    authorizedClientManager);

            requestInterceptor.setClientRegistrationIdResolver(clientRequest -> S2S_CLIENT_REGISTRATION_ID);

            return RestClient.builder().requestInterceptor(requestInterceptor);
        }

        @Bean("microServiceRestClient")
        public RestClient microServiceRestClient(
                @Qualifier("microServiceRestClientBuilder") RestClient.Builder microServiceRestClientBuilder) {
            return microServiceRestClientBuilder.build();
        }

        @Bean("defaultRestClient")
        public RestClient defaultRestClient(@Qualifier("defaultRestClientBuilder") RestClient.Builder defaultRestClientBuilder) {
            return defaultRestClientBuilder.build();
        }

        @Bean
        public RestClientBuilder restClientBuilder(Environment environment,
                                                   @Qualifier("microServiceRestClient") RestClient microServiceRestClient,
                                                   @Qualifier("defaultRestClient") RestClient defaultRestClient,
                                                   ServiceDomainProperties serviceDomainProperties) {
            return new RestClientBuilder(environment, microServiceRestClient.mutate(), defaultRestClient.mutate(), serviceDomainProperties);
        }

    }

}
