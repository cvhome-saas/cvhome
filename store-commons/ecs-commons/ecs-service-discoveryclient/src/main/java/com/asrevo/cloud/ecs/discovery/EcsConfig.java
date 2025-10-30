package com.asrevo.cloud.ecs.discovery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.cloud.client.ConditionalOnDiscoveryHealthIndicatorEnabled;
import org.springframework.cloud.client.discovery.health.DiscoveryClientHealthIndicatorProperties;
import org.springframework.cloud.client.discovery.health.reactive.ReactiveDiscoveryClientHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryAsyncClient;
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryClient;

@Configuration
@Slf4j
public class EcsConfig {

	@ConditionalOnEcsDiscoveryEnabled
	@ConditionalOnProperty(prefix = "spring.cloud.ecs.discovery", name = "namespace")
	@Configuration
	@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
	public static class EcsReactiveDiscoveryClientAutoConfiguration {

		@Bean
		public ServiceDiscoveryAsyncClient awsServiceDiscoveryAsync() {
			return ServiceDiscoveryAsyncClient.create();
		}

		@Bean
		@ConditionalOnMissingBean
		public EcsReactiveDiscoveryClient reactiveDiscoveryClient(EcsDiscoveryProperties ecsDiscoveryProperties,
				ServiceDiscoveryAsyncClient discoveryAsync) {
			return new EcsReactiveDiscoveryClient(ecsDiscoveryProperties, discoveryAsync);
		}

		@Bean
		@ConditionalOnClass(name = "org.springframework.boot.actuate.health.ReactiveHealthIndicator")
		@ConditionalOnDiscoveryHealthIndicatorEnabled
		public ReactiveDiscoveryClientHealthIndicator ecsReactiveDiscoveryClientHealthIndicator(
				EcsReactiveDiscoveryClient client, DiscoveryClientHealthIndicatorProperties properties) {
			return new ReactiveDiscoveryClientHealthIndicator(client, properties);
		}

	}

	@ConditionalOnEcsDiscoveryEnabled
	@ConditionalOnProperty(prefix = "spring.cloud.ecs.discovery", name = "namespace")
	@Configuration
	@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
	public static class EcsDiscoveryClientAutoConfiguration {

		@Bean
		public ServiceDiscoveryClient awsServiceDiscovery() {
			return ServiceDiscoveryClient.create();
		}

		@Bean
		@ConditionalOnMissingBean
		public EcsDiscoveryClient reactiveDiscoveryClient(EcsDiscoveryProperties ecsDiscoveryProperties,
				ServiceDiscoveryClient discovery) {
			return new EcsDiscoveryClient(ecsDiscoveryProperties, discovery);
		}

		/*
		 * @Bean
		 *
		 * @ConditionalOnClass(name =
		 * "org.springframework.boot.actuate.health.HealthIndicator")
		 *
		 * @ConditionalOnDiscoveryHealthIndicatorEnabled public
		 * DiscoveryClientHealthIndicator
		 * ecsReactiveDiscoveryClientHealthIndicator(ObjectProvider<DiscoveryClient>
		 * client, DiscoveryClientHealthIndicatorProperties properties) { return new
		 * DiscoveryClientHealthIndicator(client, properties); }
		 */

	}

}
