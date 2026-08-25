package com.asrevo.cvhome.gateway.client;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A gateway serving stale routes must not look healthy: DOWN before the first successful refresh and once the last one
 * is older than the threshold; briefly stale is by design and stays UP.
 */
class PodRoutesHealthIndicatorTest {

    private static final String ROUTES = "routes";

    private final PodClient podClient = mock(PodClient.class);

    private PodRoutesHealthIndicator indicator;

    @BeforeEach
    void setUp() {
        indicator = new PodRoutesHealthIndicator(podClient);
        ReflectionTestUtils.setField(indicator, "staleAfter", Duration.ofMinutes(10));
        when(podClient.knownRouteCount()).thenReturn(2);
    }

    @Test
    void downUntilTheFirstRefreshSucceeds() {
        when(podClient.timeSinceLastSuccessfulRefresh()).thenReturn(Optional.empty());

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("servingConfigSeededRoutes", 2).containsKey("reason");
    }

    @Test
    void upWhileTheLastRefreshIsRecent() {
        when(podClient.timeSinceLastSuccessfulRefresh()).thenReturn(Optional.of(Duration.ofSeconds(30)));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry(ROUTES, 2)
                .containsEntry("secondsSinceLastRefresh", 30L)
                .containsEntry("stalenessThresholdSeconds", 600L);
    }

    @Test
    void downOnceTheRoutesAreOlderThanTheThreshold() {
        when(podClient.timeSinceLastSuccessfulRefresh()).thenReturn(Optional.of(Duration.ofMinutes(11)));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry(ROUTES, 2);
    }

}
