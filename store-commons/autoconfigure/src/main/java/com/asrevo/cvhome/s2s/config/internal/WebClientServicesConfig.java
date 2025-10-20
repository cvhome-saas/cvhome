package com.asrevo.cvhome.s2s.config.internal;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.REACTIVE;
import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import com.asrevo.cvhome.s2s.oauth2.PasswordTokenResponseClient;
import com.asrevo.cvhome.s2s.oauth2.RefreshTokenTokenResponseClient;
import com.asrevo.cvhome.s2s.oauth2.ServerCallBearerExchangeFilterFunction;
import com.asrevo.cvhome.s2s.oauth2.ServerCallBearerExchangeInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesMapper;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.endpoint.WebClientReactiveClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
@Import({ WebClientServicesConfig.ReactiveWebClientServicesConfig.class,
		WebClientServicesConfig.ServletWebClientServicesConfig.class })
public class WebClientServicesConfig {

	@Configuration
	@ConditionalOnWebApplication(type = REACTIVE)
	static class ReactiveWebClientServicesConfig {

		// @TODO CHECK DefaultClientCredentialsTokenResponseClient

		/**
		 * used for inside and outside cluster call for secured c-2-s calls without client
		 * load balancing
		 */
		@Bean("webBuilder")
		@LoadBalanced
		public WebClient.Builder webBuilder(ReactiveClientRegistrationRepository clientRegistrationRepository,
				ServerOAuth2AuthorizedClientRepository serverOAuth2AuthorizedClientRepository) {
			ServerOAuth2AuthorizedClientExchangeFilterFunction oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(
					clientRegistrationRepository,
					serverOAuth2AuthorizedClientRepository /*
															 * new
															 * UnAuthenticatedServerOAuth2AuthorizedClientRepository
															 * ()
															 */);
			oauth.setDefaultClientRegistrationId("keycloak");
			return WebClient.builder().filter(oauth);
		}

		/**
		 * used for inside cluster call for secured c-2-s calls with client load balancing
		 */
		@Bean("defaultWebBuilder")
		@LoadBalanced
		public WebClient.Builder defaultWebBuilder(ReactiveClientRegistrationRepository clientRegistrationRepository,
				ServerOAuth2AuthorizedClientRepository serverOAuth2AuthorizedClientRepository) {
			ServerOAuth2AuthorizedClientExchangeFilterFunction oauth = new ServerOAuth2AuthorizedClientExchangeFilterFunction(
					clientRegistrationRepository,
					serverOAuth2AuthorizedClientRepository /*
															 * new
															 * UnAuthenticatedServerOAuth2AuthorizedClientRepository
															 * ()
															 */);
			oauth.setDefaultClientRegistrationId("keycloak");
			return WebClient.builder().filter(oauth);
		}

		/**
		 * used for inside and outside cluster call for secured s-2-s calls without client
		 * load balancing
		 */
		@Bean("defaultWebMicroServiceBuilder")
		public WebClient.Builder defaultWebMicroServiceBuilder(
				WebClientReactiveClientCredentialsTokenResponseClient tokenClient,
				ReactiveClientRegistrationRepository repository) {
			ServerCallBearerExchangeFilterFunction filter = new ServerCallBearerExchangeFilterFunction(tokenClient,
					repository, "s2s");
			return WebClient.builder().filter(filter);
		}

		/**
		 * used for inside cluster call for secured s-2-s calls with client load balancing
		 */
		@Bean("defaultMicroServiceBuilder")
		@LoadBalanced
		public WebClient.Builder defaultMicroServiceBuilder(
				WebClientReactiveClientCredentialsTokenResponseClient tokenClient,
				ReactiveClientRegistrationRepository repository) {
			ServerCallBearerExchangeFilterFunction filter = new ServerCallBearerExchangeFilterFunction(tokenClient,
					repository, "s2s");
			return WebClient.builder().filter(filter);
		}

		/**
		 * used for inside and outside cluster call for non-secured calls without client
		 * load balancing
		 */
		@Bean("defaultBuilder")
		public WebClient.Builder defaultBuilder() {
			return WebClient.builder();
		}

		/**
		 * used for inside cluster call for non-secured calls with client load balancing
		 */
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
		public RestTemplate restTemplate(PasswordTokenResponseClient responseClient,
				RefreshTokenTokenResponseClient refreshTokenTokenResponseClient, OAuth2ClientProperties properties) {
			RestTemplate restTemplate = new RestTemplate();
			ClientRegistrationRepository registrationRepository = getClientRegistrationRepository(properties);
			restTemplate.setInterceptors(
					List.of(new ServerCallBearerExchangeInterceptor(responseClient, refreshTokenTokenResponseClient,
							registrationRepository, "microservice", "microservice-gateway", "microservice-gateway")));
			return restTemplate;
		}

		@Bean("microClientBuilder")
		@LoadBalanced
		public RestClient.Builder microClientBuilder(PasswordTokenResponseClient responseClient,
				RefreshTokenTokenResponseClient refreshTokenTokenResponseClient, OAuth2ClientProperties properties) {
			ClientRegistrationRepository registrationRepository = getClientRegistrationRepository(properties);
			ServerCallBearerExchangeInterceptor e1 = new ServerCallBearerExchangeInterceptor(responseClient,
					refreshTokenTokenResponseClient, registrationRepository, "microservice", "microservice-gateway",
					"microservice-gateway");
			return RestClient.builder().requestInterceptor(e1);
		}

		@Bean
		public WebClientReactiveClientCredentialsTokenResponseClient reactivePasswordTokenResponseClient(
				@Qualifier("defaultBuilder") WebClient.Builder defaultBuilder) {
			WebClientReactiveClientCredentialsTokenResponseClient client = new WebClientReactiveClientCredentialsTokenResponseClient();
			client.setWebClient(defaultBuilder.build());
			return client;
		}

		/*
		 * issue with this i cant disable https validation and the validation fail
		 *
		 * @Bean public DefaultReactiveOAuth2UserService
		 * defaultReactiveOAuth2UserService(WebClient.Builder defaultMicroServiceBuilder)
		 * { DefaultReactiveOAuth2UserService defaultReactiveOAuth2UserService = new
		 * DefaultReactiveOAuth2UserService();
		 * defaultReactiveOAuth2UserService.setWebClient(defaultMicroServiceBuilder.build(
		 * )); return defaultReactiveOAuth2UserService; }
		 */

		/*
		 * @Bean public WebClientReactiveAuthorizationCodeTokenResponseClient
		 * webClientReactiveAuthorizationCodeTokenResponseClient(WebClient.Builder
		 * defaultBalancedBuilder) { WebClientReactiveAuthorizationCodeTokenResponseClient
		 * webClientReactiveAuthorizationCodeTokenResponseClient = new
		 * WebClientReactiveAuthorizationCodeTokenResponseClient();
		 * webClientReactiveAuthorizationCodeTokenResponseClient.setWebClient(
		 * defaultBalancedBuilder.build()); return
		 * webClientReactiveAuthorizationCodeTokenResponseClient; }
		 */
		@Bean
		public WebClientBuilder webClientBuilder(Environment environment,
				@Qualifier("defaultMicroServiceBuilder") WebClient.Builder defaultMicroServiceBuilder,
				ServiceDomainProperties serviceDomainProperties, ObjectMapper objectMapper) {
			return new WebClientBuilder(environment, defaultMicroServiceBuilder, serviceDomainProperties, objectMapper);
		}

	}

	@Configuration
	@ConditionalOnWebApplication(type = SERVLET)
	static class ServletWebClientServicesConfig {

		@Bean
		public RestClientBuilder restClientBuilder(Environment environment,
				@Qualifier("restBClientBuilder") RestClient.Builder restBClientBuilder,
				ServiceDomainProperties serviceDomainProperties) {
			return new RestClientBuilder(environment, restBClientBuilder, serviceDomainProperties);
		}

		@Bean("restBClientBuilder")
		@LoadBalanced
		public RestClient.Builder restBClientBuilder() {
			return RestClient.builder();
		}

	}

}
