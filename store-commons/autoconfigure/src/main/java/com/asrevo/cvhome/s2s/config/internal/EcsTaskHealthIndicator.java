package com.asrevo.cvhome.s2s.config.internal;

import java.util.Map;
import java.util.function.Supplier;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

import com.asrevo.cvhome.fargate.task.EcsTask;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EcsTaskHealthIndicator implements HealthIndicator {

    static final String NOT_ON_ECS = "not an ECS task";

    private final Supplier<EcsTask> ecsTask;

    private final ObjectMapper objectMapper;

    /**
     * @param ecsTask the task this process runs as, or {@code null} when it is not an ECS task
     */
    public EcsTaskHealthIndicator(Supplier<EcsTask> ecsTask, ObjectMapper objectMapper) {
        this.ecsTask = ecsTask;
        this.objectMapper = objectMapper;
    }

    @Override
    public Health health() {
        if (ecsTask == null) {
            return Health.up().withDetail("ecs", NOT_ON_ECS).build();
        }
        Health.Builder status = Health.up();
        try {
            status.withDetails(objectMapper.convertValue(ecsTask.get(), new TypeReference<Map<String, Object>>() {
            }));
        } catch (Exception e) {
            status = Health.down(e);
            log.error("error EcsTaskHealthIndicator", e);
        }
        return status.build();
    }

}
