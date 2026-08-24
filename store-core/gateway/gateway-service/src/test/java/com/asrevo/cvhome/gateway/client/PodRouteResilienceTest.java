package com.asrevo.cvhome.gateway.client;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import com.asrevo.cvhome.commons.domain.EndpointType;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodEndpoint;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.podregistry.api.ReactiveExternalPodService;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;

import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The failure this guards against is total tenant downtime, not a degraded feature: when route lookup did its own I/O
 * and swallowed errors into an empty result, one registry restart emptied the gateway's whole pod route table and
 * every {@code /spg/**} storefront request 404'd within a refresh period.
 */
class PodRouteResilienceTest {

    private static final String POD_A_ROUTE_ID = "pod-507f1f77";

    private static final Pod POD_A = pod("507f1f77bcf86cd799439011", "pod-a", "http://pod-a.example");

    private static final Pod POD_B = pod("607f1f77bcf86cd799439012", "pod-b", "http://pod-b.example");

    private ReactiveExternalPodService podService;

    private ApplicationEventPublisher publisher;

    private PodClient podClient;

    private static Pod pod(String id, String name, String endpoint) {
        return new Pod(new PodId(id), name, new PodEndpoint(endpoint, EndpointType.EXTERNAL),
                new ManagerOrgId("21f023932bc66470c104b76f"), null);
    }

    private PodClient buildPodClient(List<Pod> configuredPods) {
        Environment environment = new MockEnvironment().withProperty("spring.application.name", "store-core-gateway");
        ServiceDomainProperties properties = new ServiceDomainProperties(Map.of(), configuredPods);
        return new PodClient(properties, podService, environment, publisher);
    }

    private List<String> routeIds(PodClient client) {
        return client.getRouteDefinitions().map(RouteDefinition::getId).collectList().block();
    }

    @BeforeEach
    void setUp() {
        podService = mock(ReactiveExternalPodService.class);
        publisher = mock(ApplicationEventPublisher.class);
        podClient = buildPodClient(List.of(POD_A));
        podClient.seedFromConfiguration();
    }

    @Test
    @DisplayName("a failed refresh leaves the existing routes in place")
    void failedRefreshKeepsLastKnownGood() {
        when(podService.listPods()).thenReturn(Mono.error(new IllegalStateException("registry is down")));

        podClient.refreshRoutes();

        assertThat(routeIds(podClient)).containsExactly(POD_A_ROUTE_ID);
        assertThat(podClient.knownRouteCount()).isOne();
        // Nothing was published, so CachingRouteLocator never rebuilds its table from an error.
        verify(publisher, never()).publishEvent(any(RefreshRoutesEvent.class));
    }

    @Test
    @DisplayName("an empty response is honoured — that is a real answer, not a failure")
    void emptyResponseIsAppliedButOnlyWhenItIsTheRealAnswer() {
        when(podService.listPods()).thenReturn(Mono.just(List.of()));

        podClient.refreshRoutes();

        assertThat(routeIds(podClient)).isEmpty();
    }

    @Test
    @DisplayName("routes are replaced and a refresh is published only when they actually change")
    void publishesOnlyOnChange() {
        when(podService.listPods()).thenReturn(Mono.just(List.of(POD_A, POD_B)));

        podClient.refreshRoutes();
        assertThat(routeIds(podClient)).containsExactly(POD_A_ROUTE_ID, "pod-607f1f77");
        verify(publisher, times(1)).publishEvent(any(RefreshRoutesEvent.class));

        // Same answer again: no event, because republishing churned the whole route cache every minute for nothing.
        podClient.refreshRoutes();
        verify(publisher, times(1)).publishEvent(any(RefreshRoutesEvent.class));
    }

    @Test
    @DisplayName("route lookup performs no I/O, so the registry cannot be in the request path")
    void lookupDoesNotCallTheRegistry() {
        routeIds(podClient);

        verify(podService, never()).listPods();
    }

    @Test
    @DisplayName("with no pods configured the seed is empty rather than exploding")
    void seedingToleratesNoConfiguredPods() {
        PodClient unseeded = buildPodClient(List.of());

        unseeded.seedFromConfiguration();

        assertThat(routeIds(unseeded)).isEmpty();
        assertThat(unseeded.timeSinceLastSuccessfulRefresh()).isEmpty();
    }

}
