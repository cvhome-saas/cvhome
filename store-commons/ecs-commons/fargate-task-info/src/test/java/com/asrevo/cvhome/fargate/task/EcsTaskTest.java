package com.asrevo.cvhome.fargate.task;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reading the task-metadata document ECS serves to a running Fargate container.
 *
 * <p>
 * The payload is not ours: it is whatever the container agent publishes, capitalized keys and all, and it grows new
 * fields between agent versions. So the two things worth proving are that the nested shape binds at all, and that an
 * unrecognised field does not turn a health probe into an exception.
 * </p>
 */
class EcsTaskTest {

    private static final String CATALOG = "catalog";

    private static final String RUNNING = "RUNNING";

    private static EcsTask parse(String json) {
        return new ObjectMapper().readValue(json, EcsTask.class);
    }

    private static String sample() throws IOException {
        try (InputStream in = EcsTaskTest.class.getResourceAsStream("/task-metadata.json")) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void theTaskLevelFieldsBindFromTheCapitalizedKeysEcsUses() throws IOException {
        EcsTask task = parse(sample());

        assertThat(task.getFamily()).isEqualTo(CATALOG);
        assertThat(task.getRevision()).isEqualTo("7");
        assertThat(task.getKnownStatus()).isEqualTo(RUNNING);
        assertThat(task.getAvailabilityZone()).isEqualTo("eu-west-1a");
        assertThat(task.getLaunchType()).isEqualTo("FARGATE");
    }

    @Test
    void theTaskLimitsBind() throws IOException {
        EcsTask task = parse(sample());

        assertThat(task.getLimits().getCpu()).isEqualTo(1.0);
        assertThat(task.getLimits().getMemory()).isEqualTo(2048);
    }

    @Test
    void theContainerAndItsNestedNetworkAndHealthBind() throws IOException {
        Container container = parse(sample()).getContainers().getFirst();

        assertThat(container.getName()).isEqualTo(CATALOG);
        assertThat(container.getKnownStatus()).isEqualTo(RUNNING);
        assertThat(container.getNetworks().getFirst().getNetworkMode()).isEqualTo("awsvpc");
        assertThat(container.getNetworks().getFirst().getIPv4Addresses()).containsExactly("10.0.1.7");
        assertThat(container.getHealth().getStatus()).isEqualTo("HEALTHY");
    }

    @Test
    void theClockDriftAndStorageMetricsBind() throws IOException {
        EcsTask task = parse(sample());

        assertThat(task.getClockDrift().getClockSynchronizationStatus()).isEqualTo("SYNCHRONIZED");
        assertThat(task.getClockDrift().getClockErrorBound()).isEqualTo(0.4);
        assertThat(task.getEphemeralStorageMetrics().getUtilized()).isEqualTo(261);
        assertThat(task.getEphemeralStorageMetrics().getReserved()).isEqualTo(20496);
    }

    /**
     * The agent adds fields between versions, and the fetcher has no status-code check and no explicit
     * FAIL_ON_UNKNOWN_PROPERTIES setting — {@code EcsTaskFetcher.getObjectMapper} queries that flag and discards the
     * answer. This pins the behaviour the fetcher actually depends on.
     */
    @Test
    void aFieldThisVersionDoesNotKnowAboutIsIgnoredRatherThanFatal() {
        EcsTask task = parse("{\"Family\":\"catalog\",\"SomethingNewInTheAgent\":true}");

        assertThat(task.getFamily()).isEqualTo(CATALOG);
    }

    @Test
    void anEmptyDocumentBindsToATaskWithNothingSetRatherThanFailing() {
        EcsTask task = parse("{}");

        assertThat(task.getFamily()).isNull();
        assertThat(task.getContainers()).isNull();
    }
}
