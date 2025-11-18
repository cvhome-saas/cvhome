package com.asrevo.cvhome.s2s.config.internal;

import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.REACTIVE;
import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

@Configuration
@Slf4j
@Import({ WebClientServicesConfig.ReactiveWebClientServicesConfig.class,
		WebClientServicesConfig.ServletWebClientServicesConfig.class })
public class WebClientServicesConfig {

	@Configuration
	@ConditionalOnWebApplication(type = REACTIVE)
	static class ReactiveWebClientServicesConfig {

		@Bean("defaultMicroServiceBuilder")
		@LoadBalanced
		public WebClient.Builder defaultMicroServiceBuilder(
				AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
			ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Filter = new ServerOAuth2AuthorizedClientExchangeFilterFunction(
					authorizedClientManager);
			oauth2Filter.setDefaultClientRegistrationId("s2s");

			return WebClient.builder().filter(oauth2Filter);
		}

		@Bean("defaultBuilder")
		public WebClient.Builder defaultBuilder(
				AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
			ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Filter = new ServerOAuth2AuthorizedClientExchangeFilterFunction(
					authorizedClientManager);
			oauth2Filter.setDefaultClientRegistrationId("s2s");

			return WebClient.builder().filter(oauth2Filter);
		}

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
				@Qualifier("microClientBuilder") RestClient.Builder microClientBuilder,
				ServiceDomainProperties serviceDomainProperties) {
			return new RestClientBuilder(environment, microClientBuilder, serviceDomainProperties);
		}

		@Bean("microClientBuilder")
		@LoadBalanced
		public RestClient.Builder microClientBuilder(
				AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager) {

			OAuth2ClientHttpRequestInterceptor requestInterceptor = new OAuth2ClientHttpRequestInterceptor(
					authorizedClientManager);

			requestInterceptor.setClientRegistrationIdResolver(clientRequest -> "s2s");

			return RestClient.builder().requestInterceptor(requestInterceptor);
		}

	}

}
