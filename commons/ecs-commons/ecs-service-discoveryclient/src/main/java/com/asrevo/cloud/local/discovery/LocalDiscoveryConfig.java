package com.asrevo.cloud.local.discovery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.ConditionalOnDiscoveryHealthIndicatorEnabled;
import org.springframework.cloud.client.discovery.health.DiscoveryClientHealthIndicatorProperties;
import org.springframework.cloud.client.discovery.health.reactive.ReactiveDiscoveryClientHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ashraf
 * configration for local discovery
 */
@Configuration
@Slf4j
public class LocalDiscoveryConfig {
    /**
     * @author ashraf
     * auto configration for local discovery
     */
    @ConditionalOnLocalDiscoveryEnabled
    @Configuration
    public static class LocalReactiveDiscoveryClientAutoConfiguration {

        /**
         * used to define bean for LocalReactiveDiscoveryClient
         *
         * @param localDiscoveryProperties used to configure the client
         * @return LocalReactiveDiscoveryClient bean
         */
        @Bean
        @ConditionalOnMissingBean
        public LocalReactiveDiscoveryClient reactiveDiscoveryClient(LocalDiscoveryProperties localDiscoveryProperties) {
            return new LocalReactiveDiscoveryClient(localDiscoveryProperties);
        }

        /**
         * used to define bean for LocalDiscoveryClient
         *
         * @param localDiscoveryProperties used to configure the client
         * @return LocalDiscoveryClient bean
         */
        @Bean
        @ConditionalOnMissingBean
        public LocalDiscoveryClient discoveryClient(LocalDiscoveryProperties localDiscoveryProperties) {
            return new LocalDiscoveryClient(localDiscoveryProperties);
        }

        /**
         * used to define bean for ReactiveDiscoveryClientHealthIndicator
         *
         * @param client     use client to do health check
         * @param properties used to get current properties
         * @return ReactiveDiscoveryClientHealthIndicator bean
         */
        @Bean
        @ConditionalOnClass(name = "org.springframework.boot.actuate.health.ReactiveHealthIndicator")
        @ConditionalOnDiscoveryHealthIndicatorEnabled
        public ReactiveDiscoveryClientHealthIndicator localReactiveDiscoveryClientHealthIndicator(LocalReactiveDiscoveryClient client, DiscoveryClientHealthIndicatorProperties properties) {
            return new ReactiveDiscoveryClientHealthIndicator(client, properties);
        }
    }

}
