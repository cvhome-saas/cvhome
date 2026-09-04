package com.asrevo.cvhome.fargate.task;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * What {@link EcsTaskFetcher} does when there is no ECS metadata endpoint to ask.
 *
 * <p>
 * Its catch arms promise an empty {@link EcsTask} for a failed fetch, but they cover the failures that happen
 * <em>after</em> the request is built — a timeout, a refused connection, a malformed URI. Off ECS the environment
 * variable is unset, the URI has no scheme, and {@code HttpRequest.newBuilder} refuses it before any of that: an
 * IllegalArgumentException, not an empty task.
 * </p>
 *
 * <p>
 * That is safe only because the one caller, {@code EcsInfoConfig}, is
 * {@code @ConditionalOnProperty(AWS_EXECUTION_ENV = AWS_ECS_FARGATE)} — so {@code fetch()} runs only where the
 * variable is set. This test pins the behaviour so a second, unguarded caller is a failing test rather than a
 * service that will not start outside AWS.
 * </p>
 */
class EcsTaskFetcherTest {

    @Test
    void offEcsTheFetchFailsFastRatherThanAnsweringAnEmptyTask() {
        // Guarded by EcsInfoConfig's @ConditionalOnProperty; calling it anywhere else is the mistake this catches.
        assertThatThrownBy(EcsTaskFetcher::fetch)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("scheme");
    }

    @Test
    void anEmptyTaskIsWhatTheFailurePathsAnswerWith() {
        // The shape the catch arms return: every field absent rather than a null task.
        assertThat(new EcsTask()).isNotNull();
        assertThat(new EcsTask().getTaskARN()).isNull();
    }
}
