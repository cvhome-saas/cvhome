package com.asrevo.cvhome.gateway.client;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.env.MockEnvironment;

import com.asrevo.cvhome.commons.domain.EndpointType;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodEndpoint;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.podregistry.api.ReactiveExternalPodService;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The shape of a pod route: {@code /spg/**} plus the {@code store} and exact {@code pod} query parameters, one strip
 * of the prefix and a token relay, aimed at the pod's endpoint (through the load balancer for internal pods).
 */
class PodClientTest {

    private static final String POD_ID = "507f1f77bcf86cd799439011";

    private static final String ORG_ID = "21f023932bc66470c104b76f";

    private static final String ENDPOINT = "http://pod-a.example";

    private static final String QUERY = "Query";

    private final ReactiveExternalPodService podService = mock(ReactiveExternalPodService.class);

    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);

    private static Pod pod(String endpoint, EndpointType type) {
        return new Pod(new PodId(POD_ID), "pod-a", new PodEndpoint(endpoint, type), new ManagerOrgId(ORG_ID), null);
    }

    private PodClient client(Pod... pods) {
        ServiceDomainProperties properties = new ServiceDomainProperties(Map.of(), List.of(pods));
        return new PodClient(properties, podService,
                new MockEnvironment().withProperty("spring.application.name", "store-core-gateway"), publisher);
    }

    @Test
    void externalPodRouteCarriesPathStoreAndExactPodPredicates() {
        PodClient client = client();
        when(podService.listPods()).thenReturn(Mono.just(List.of(pod(ENDPOINT, EndpointType.EXTERNAL))));

        client.refreshRoutes();
        RouteDefinition route = client.getRouteDefinitions().blockFirst();

        assertThat(route).isNotNull();
        assertThat(route.getId()).isEqualTo("pod-507f1f77");
        assertThat(route.getUri()).hasToString(ENDPOINT);
        assertThat(route.getPredicates()).extracting(PredicateDefinition::getName).containsExactly("Path", QUERY, QUERY);
        assertThat(route.getPredicates().get(0).getArgs()).containsValue("/spg/**");
        assertThat(route.getPredicates().get(1).getArgs()).containsValue("store");
        assertThat(route.getPredicates().get(2).getArgs()).containsValues("pod", POD_ID);
        assertThat(route.getFilters()).extracting(FilterDefinition::getName).containsExactly("StripPrefix", "TokenRelay");
        assertThat(client.timeSinceLastSuccessfulRefresh()).isPresent();
    }

    @Test
    void internalPodIsReachedThroughTheLoadBalancer() {
        PodClient client = client(pod("pod-a.internal", EndpointType.INTERNAL));

        client.seedFromConfiguration();

        assertThat(client.getRouteDefinitions().blockFirst().getUri()).hasToString("lb://spg.pod-a.internal");
        assertThat(client.knownRouteCount()).isOne();
    }

    @Test
    void saveAndDeleteAreNoOps() {
        PodClient client = client();

        StepVerifier.create(client.save(Mono.just(new RouteDefinition()))).verifyComplete();
        StepVerifier.create(client.delete(Mono.just("pod-x"))).verifyComplete();
        assertThat(client.knownRouteCount()).isZero();
    }

}
