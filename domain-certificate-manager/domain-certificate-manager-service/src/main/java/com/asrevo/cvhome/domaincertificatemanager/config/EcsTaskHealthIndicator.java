package com.asrevo.cvhome.domaincertificatemanager.config;

import com.asrevo.cvhome.fargate.task.EcsTask;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
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
            status.withDetails(objectMapper.convertValue(ecsTask,
                    new TypeReference<Map<String, Object>>() {
                    }));
        } catch (Exception e) {
            status = Health.down(e);
            log.error("error EcsTaskHealthIndicator", e);
        }
        return status.build();
    }
}
