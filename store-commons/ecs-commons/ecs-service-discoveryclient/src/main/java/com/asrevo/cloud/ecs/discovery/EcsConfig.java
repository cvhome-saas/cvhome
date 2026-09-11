package com.asrevo.cloud.ecs.discovery;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.cloud.client.ConditionalOnDiscoveryHealthIndicatorEnabled;
import org.springframework.cloud.client.discovery.health.DiscoveryClientHealthIndicatorProperties;
import org.springframework.cloud.client.discovery.health.reactive.ReactiveDiscoveryClientHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.function.SingletonSupplier;

import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryAsyncClient;
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryClient;

/**
 * Cloud Map discovery, which the Fargate deployment resolves every {@code lb://} name through.
 *
 * <p>
 * The client is a bean in every deployment and consults Cloud Map only where {@code spring.cloud.ecs.discovery.enabled}
 * is true and a namespace is set — {@code fargate-config.yml} and the task's environment. Everywhere else it knows no
 * services, and the composite discovery client falls through to the simple one the {@code lcl} slice configures, as
 * it did when this was a conditional bean. It stopped being one because a native image fixes its beans once, when it
 * is built, and the same image runs on Fargate and on the load-testing stack.
 * </p>
 *
 * <p>
 * The AWS SDK client is built on first use, not with the bean: building it resolves a region and credentials, and a
 * laptop running the {@code lcl} profile has neither.
 * </p>
 */
@Configuration
@SuppressWarnings("java:S1118")
public class EcsConfig {

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public static class EcsReactiveDiscoveryClientAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public EcsReactiveDiscoveryClient reactiveDiscoveryClient(EcsDiscoveryProperties ecsDiscoveryProperties) {
            return new EcsReactiveDiscoveryClient(ecsDiscoveryProperties,
                    SingletonSupplier.of(ServiceDiscoveryAsyncClient::create));
        }

        @Bean
        @ConditionalOnClass(name = "org.springframework.boot.actuate.health.ReactiveHealthIndicator")
        @ConditionalOnDiscoveryHealthIndicatorEnabled
        public ReactiveDiscoveryClientHealthIndicator ecsReactiveDiscoveryClientHealthIndicator(
                EcsReactiveDiscoveryClient client, DiscoveryClientHealthIndicatorProperties properties) {
            return new ReactiveDiscoveryClientHealthIndicator(client, properties);
        }

    }

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public static class EcsDiscoveryClientAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public EcsDiscoveryClient reactiveDiscoveryClient(EcsDiscoveryProperties ecsDiscoveryProperties) {
            return new EcsDiscoveryClient(ecsDiscoveryProperties, SingletonSupplier.of(ServiceDiscoveryClient::create));
        }

    }

}
