package com.asrevo.cvhome.s2s.config.internal;

import java.util.function.Supplier;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.function.SingletonSupplier;

import com.asrevo.cvhome.fargate.task.EcsTask;
import com.asrevo.cvhome.fargate.task.EcsTaskFetcher;

import tools.jackson.databind.ObjectMapper;

/**
 * The ECS task this process runs as, on the health endpoint.
 *
 * <p>
 * Decided when the process starts, not when it is built: Fargate sets {@value #EXECUTION_ENV} in every task's
 * environment, and a native image fixes its beans at build time, where that variable never exists — a condition on it
 * would have left the indicator out of every image. Off Fargate the indicator reports UP and says so.
 * </p>
 *
 * <p>
 * {@link EcsTask} is bound by Jackson twice — read from the task metadata endpoint, written back out as health
 * details — so a native image needs its reflection metadata, which nothing else would register.
 * </p>
 */
@Configuration
@RegisterReflectionForBinding(EcsTask.class)
public class EcsInfoConfig {

    static final String EXECUTION_ENV = "AWS_EXECUTION_ENV";

    static final String FARGATE = "AWS_ECS_FARGATE";

    @Bean
    public EcsTaskHealthIndicator ecsTaskHealthIndicator(Environment environment, ObjectMapper objectMapper) {
        return new EcsTaskHealthIndicator(ecsTask(environment), objectMapper);
    }

    /**
     * The task metadata, fetched once on first use; {@code null} when this process is not a Fargate task.
     */
    static Supplier<EcsTask> ecsTask(Environment environment) {
        return FARGATE.equals(environment.getProperty(EXECUTION_ENV)) ? SingletonSupplier.of(EcsTaskFetcher::fetch) : null;
    }

}
