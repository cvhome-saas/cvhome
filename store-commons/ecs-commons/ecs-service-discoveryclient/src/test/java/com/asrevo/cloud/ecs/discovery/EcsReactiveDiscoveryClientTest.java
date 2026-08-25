package com.asrevo.cloud.ecs.discovery;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.client.ServiceInstance;

import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryAsyncClient;
import software.amazon.awssdk.services.servicediscovery.model.DiscoverInstancesRequest;
import software.amazon.awssdk.services.servicediscovery.model.DiscoverInstancesResponse;
import software.amazon.awssdk.services.servicediscovery.model.HttpInstanceSummary;
import software.amazon.awssdk.services.servicediscovery.model.ListServicesRequest;
import software.amazon.awssdk.services.servicediscovery.model.ListServicesResponse;
import software.amazon.awssdk.services.servicediscovery.model.ServiceSummary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The gateway's half of ECS discovery: the same Cloud Map lookups, returned as a {@code Flux}.
 *
 * <p>
 * It is deliberately <em>not</em> a mirror of the blocking client, and the difference is worth pinning: this one
 * never splits a qualified service id, so {@code catalog.pods.local} is looked up under that whole name in the
 * configured namespace. A test that assumed the two behaved alike would be asserting something the gateway does not
 * do.
 * </p>
 */
class EcsReactiveDiscoveryClientTest {

    private static final String IPV4 = "AWS_INSTANCE_IPV4";

    private static final String CATALOG = "catalog";

    private static final String NAMESPACE = "pods.local";

    private static final String NAMESPACE_ID = "ns-abc";

    private static final String ALWAYS_PRESENT = "uaa";

    private static final Integer DEFAULT_PORT = 8080;

    private static final String HOST = "10.0.1.7";

    private static final String QUALIFIED = "%s.%s";

    private ServiceDiscoveryAsyncClient discovery;

    private EcsDiscoveryProperties properties;

    @BeforeEach
    void setUp() {
        discovery = mock(ServiceDiscoveryAsyncClient.class);
        properties = new EcsDiscoveryProperties();
        properties.setNamespace(NAMESPACE);
        properties.setDefaultPort(DEFAULT_PORT);
        when(discovery.discoverInstances(any(DiscoverInstancesRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(DiscoverInstancesResponse.builder()
                        .instances(HttpInstanceSummary.builder().serviceName(CATALOG).namespaceName(NAMESPACE)
                                .instanceId("i-1").attributes(Map.of(IPV4, HOST)).build())
                        .build()));
        when(discovery.listServices(any(ListServicesRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(ListServicesResponse.builder()
                        .services(ServiceSummary.builder().name(CATALOG).build()).build()));
    }

    @Test
    void aServiceIdIsLookedUpWholeInTheConfiguredNamespace() {
        EcsReactiveDiscoveryClient.getDefaultServiceInstances(discovery, properties,
                String.format(QUALIFIED, CATALOG, NAMESPACE)).collectList().block();

        ArgumentCaptor<DiscoverInstancesRequest> request = ArgumentCaptor.forClass(DiscoverInstancesRequest.class);
        verify(discovery).discoverInstances(request.capture());
        assertThat(request.getValue().serviceName()).isEqualTo(String.format(QUALIFIED, CATALOG, NAMESPACE));
        assertThat(request.getValue().namespaceName()).isEqualTo(NAMESPACE);
    }

    @Test
    void everyRegistrationBecomesAnInstanceOnTheDefaultPort() {
        List<ServiceInstance> instances =
                EcsReactiveDiscoveryClient.getDefaultServiceInstances(discovery, properties, CATALOG)
                        .collectList().block();

        assertThat(instances).singleElement().satisfies(instance -> {
            assertThat(instance.getHost()).isEqualTo(HOST);
            assertThat(instance.getPort()).isEqualTo(DEFAULT_PORT);
        });
    }

    @Test
    void aPerServicePortOverridesTheDefault() {
        properties.setServicePorts(Map.of(CATALOG, 9091));

        List<ServiceInstance> instances =
                EcsReactiveDiscoveryClient.getDefaultServiceInstances(discovery, properties, CATALOG)
                        .collectList().block();

        assertThat(instances).singleElement().extracting(ServiceInstance::getPort).isEqualTo(9091);
    }

    @Test
    void withoutANamespaceIdEveryServiceIsListed() {
        EcsReactiveDiscoveryClient.getEcsServices(discovery, properties).collectList().block();

        ArgumentCaptor<ListServicesRequest> request = ArgumentCaptor.forClass(ListServicesRequest.class);
        verify(discovery).listServices(request.capture());
        assertThat(request.getValue().filters()).isEmpty();
    }

    @Test
    void aConfiguredNamespaceIdBecomesAFilter() {
        properties.setNamespaceId(NAMESPACE_ID);

        EcsReactiveDiscoveryClient.getEcsServices(discovery, properties).collectList().block();

        ArgumentCaptor<ListServicesRequest> request = ArgumentCaptor.forClass(ListServicesRequest.class);
        verify(discovery).listServices(request.capture());
        assertThat(request.getValue().filters()).singleElement()
                .satisfies(filter -> assertThat(filter.values()).containsExactly(NAMESPACE_ID));
    }

    @Test
    void servicesConfiguredAsAlwaysPresentAreMergedIntoWhatCloudMapReturns() {
        properties.setIncludeServices(List.of(ALWAYS_PRESENT));

        assertThat(EcsReactiveDiscoveryClient.getEcsServices(discovery, properties).collectList().block())
                .containsExactlyInAnyOrder(CATALOG, ALWAYS_PRESENT);
    }

    @Test
    void theClientDescribesItselfForTheActuatorEndpoint() {
        assertThat(new EcsReactiveDiscoveryClient(properties, discovery).description())
                .isEqualTo("ecs reactive discovery client");
    }

    @Test
    void theInstanceMethodsDelegateToTheStaticResolution() {
        EcsReactiveDiscoveryClient client = new EcsReactiveDiscoveryClient(properties, discovery);

        assertThat(client.getInstances(CATALOG).collectList().block()).hasSize(1);
        assertThat(client.getServices().collectList().block()).containsExactly(CATALOG);
    }
}
