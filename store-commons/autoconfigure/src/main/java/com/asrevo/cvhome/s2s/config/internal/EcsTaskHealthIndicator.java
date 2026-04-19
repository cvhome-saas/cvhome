package com.asrevo.cvhome.s2s.config.internal;

import java.util.Map;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

import com.asrevo.cvhome.fargate.task.EcsTask;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EcsTaskHealthIndicator implements HealthIndicator {

    private final EcsTask ecsTask;

    private final ObjectMapper objectMapper;

    public EcsTaskHealthIndicator(EcsTask ecsTask, ObjectMapper objectMapper) {
        this.ecsTask = ecsTask;
        this.objectMapper = objectMapper;
    }

    @Override
    public Health health() {
        Health.Builder status = Health.up();
        try {
            status.withDetails(objectMapper.convertValue(ecsTask, new TypeReference<Map<String, Object>>() {
            }));
        } catch (Exception e) {
            status = Health.down(e);
            log.error("error EcsTaskHealthIndicator", e);
        }
        return status.build();
    }

}
