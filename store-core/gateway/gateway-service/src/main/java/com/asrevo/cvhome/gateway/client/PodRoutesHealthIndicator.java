package com.asrevo.cvhome.gateway.client;

import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Surfaces how stale the pod route table is.
 *
 * <p>
 * {@link PodClient} deliberately keeps serving its last known routes when a refresh fails, which means a gateway cut
 * off from the pod registry keeps working — and would otherwise look indistinguishable from a healthy one. This is
 * what makes that state visible: without it the failure is invisible until a pod moves and traffic goes nowhere.
 *
 * <p>
 * DOWN is reserved for two genuinely broken states: never having fetched successfully, or having gone longer than
 * {@code staleAfter} since the last success. Being briefly stale is by design and stays UP.
 */
@Component
@RequiredArgsConstructor
public class PodRoutesHealthIndicator implements HealthIndicator {

    private final PodClient podClient;

    @Value("${cvhome.gateway.route-staleness-threshold:PT10M}")
    private Duration staleAfter;

    @Override
    public Health health() {
        Optional<Duration> since = podClient.timeSinceLastSuccessfulRefresh();
        int routes = podClient.knownRouteCount();

        if (since.isEmpty()) {
            return Health.down()
                    .withDetail("reason", "no successful pod route refresh yet")
                    .withDetail("servingConfigSeededRoutes", routes)
                    .build();
        }

        Duration age = since.get();
        Health.Builder builder = age.compareTo(staleAfter) > 0 ? Health.down() : Health.up();
        return builder.withDetail("routes", routes)
                .withDetail("secondsSinceLastRefresh", age.toSeconds())
                .withDetail("stalenessThresholdSeconds", staleAfter.toSeconds())
                .build();
    }

}
