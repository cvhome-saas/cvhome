package com.asrevo.cvhome.gateway.config;

import com.asrevo.cvhome.fargate.task.EcsTask;
import com.asrevo.cvhome.fargate.task.EcsTaskFetcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "AWS_EXECUTION_ENV", havingValue = "AWS_ECS_FARGATE")
public class EcsInfoConfig {
    @Bean
    public EcsTask ecsTask() {
        return EcsTaskFetcher.fetch();
    }

    @Bean
    public EcsTaskHealthIndicator ecsTaskHealthIndicator(EcsTask ecsTask, ObjectMapper objectMapper) {
        return new EcsTaskHealthIndicator(ecsTask, objectMapper);
    }
}