package com.asrevo.cvhome.gateway.config;

import com.asrevo.cvhome.fargate.task.EcsTask;
import com.asrevo.cvhome.fargate.task.EcsTaskFetcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EcsInfoConfig {
    @Bean
    public EcsTask ecsTask() {
        return EcsTaskFetcher.fetch();
    }
}