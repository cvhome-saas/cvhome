package com.asrevo.cvhome.manager;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.s2s.config.ReactiveTestCustomSecurityConfig;
import com.asrevo.cvhome.subscription.api.SubscriptionPlanDetailsService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClient;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryProperties;
import org.springframework.cloud.client.discovery.simple.reactive.SimpleReactiveDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
@Import(ReactiveTestCustomSecurityConfig.class)
public class TestcontainersConfiguration {
    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"));
    }

    @Bean
    @ServiceConnection
    RabbitMQContainer rabbitContainer() {
        return new RabbitMQContainer(DockerImageName.parse("rabbitmq:4.0.2-management"));
    }

    @Bean
    public CommandLineRunner runner(SubscriptionPlanDetailsService subscriptionPlanDetailsService, SimpleDiscoveryProperties properties, SimpleReactiveDiscoveryClient simpleDiscoveryClient) {
        return args -> {
            simpleDiscoveryClient.getServices().subscribe(System.out::println);
            
            System.out.println(properties.getInstances().size());
            subscriptionPlanDetailsService.subscriptionPlanDetails(new ManagerOrgId("21f023932bc66470c104b76f")).subscribe(it -> {
                System.out.println(it);
            });
        };
    }
}
