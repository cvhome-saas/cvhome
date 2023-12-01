package com.asrevo.cloud.ecs.discovery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ConditionalOnDiscoveryHealthIndicatorEnabled;
import org.springframework.cloud.client.discovery.health.DiscoveryClientHealthIndicatorProperties;
import org.springframework.cloud.client.discovery.health.reactive.ReactiveDiscoveryClientHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryAsyncClient;

@Configuration
@Slf4j
public class EcsConfig {

    @ConditionalOnEcsDiscoveryEnabled
    @ConditionalOnProperty(prefix = "spring.cloud.ecs.discovery", name = "namespace")
    @Configuration
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
        public ReactiveDiscoveryClientHealthIndicator ecsReactiveDiscoveryClientHealthIndicator(EcsReactiveDiscoveryClient client, DiscoveryClientHealthIndicatorProperties properties) {
            return new ReactiveDiscoveryClientHealthIndicator(client, properties);
        }
    }

}
