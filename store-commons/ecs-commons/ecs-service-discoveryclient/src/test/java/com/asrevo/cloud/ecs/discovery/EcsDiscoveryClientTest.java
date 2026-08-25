package com.asrevo.cloud.ecs.discovery;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.client.ServiceInstance;

import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryClient;
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
 * Resolving a Spring service id against AWS Cloud Map.
 *
 * <p>
 * A service id may carry its own namespace ({@code catalog.pods.local}) or not ({@code catalog}), and the split is
 * what decides which namespace is queried. The malformed shapes matter as much as the well-formed one: a leading or
 * trailing dot has to fall back to the configured namespace rather than query an empty one, which returns nothing
 * and looks exactly like the service being down.
 * </p>
 */
class EcsDiscoveryClientTest {

    private static final String IPV4 = "AWS_INSTANCE_IPV4";

    private static final String CATALOG = "catalog";

    private static final String CONFIGURED_NAMESPACE = "pods.local";

    private static final String OTHER_NAMESPACE = "other.local";

    private static final Integer DEFAULT_PORT = 8080;

    private static final String QUALIFIED = "%s.%s";

    private static final String LEADING_DOT = ".%s";

    private static final String NAMESPACE_ID = "ns-abc";

    private static final String ALWAYS_PRESENT = "uaa";

    private ServiceDiscoveryClient discovery;

    private EcsDiscoveryProperties properties;

    @BeforeEach
    void setUp() {
        discovery = mock(ServiceDiscoveryClient.class);
        properties = new EcsDiscoveryProperties();
        properties.setNamespace(CONFIGURED_NAMESPACE);
        properties.setDefaultPort(DEFAULT_PORT);
        when(discovery.discoverInstances(any(DiscoverInstancesRequest.class)))
                .thenReturn(DiscoverInstancesResponse.builder().instances(instance()).build());
    }

    private static HttpInstanceSummary instance() {
        return HttpInstanceSummary.builder().serviceName(CATALOG).namespaceName(CONFIGURED_NAMESPACE)
                .instanceId("i-1").attributes(Map.of(IPV4, "10.0.1.7")).build();
    }

    private DiscoverInstancesRequest resolve(String serviceId) {
        EcsDiscoveryClient.getDefaultServiceInstances(discovery, properties, serviceId);
        ArgumentCaptor<DiscoverInstancesRequest> request = ArgumentCaptor.forClass(DiscoverInstancesRequest.class);
        verify(discovery).discoverInstances(request.capture());
        return request.getValue();
    }

    @Nested
    class ServiceIdParsing {

        @Test
        void aBareServiceIdIsLookedUpInTheConfiguredNamespace() {
            DiscoverInstancesRequest request = resolve(CATALOG);

            assertThat(request.serviceName()).isEqualTo(CATALOG);
            assertThat(request.namespaceName()).isEqualTo(CONFIGURED_NAMESPACE);
        }

        @Test
        void aQualifiedServiceIdCarriesItsOwnNamespace() {
            DiscoverInstancesRequest request = resolve(String.format(QUALIFIED, CATALOG, OTHER_NAMESPACE));

            assertThat(request.serviceName()).isEqualTo(CATALOG);
            assertThat(request.namespaceName()).isEqualTo(OTHER_NAMESPACE);
        }

        @Test
        void aTrailingDotFallsBackToTheConfiguredNamespaceRatherThanQueryingAnEmptyOne() {
            DiscoverInstancesRequest request = resolve(String.format("%s.", CATALOG));

            assertThat(request.serviceName()).isEqualTo(CATALOG);
            assertThat(request.namespaceName()).isEqualTo(CONFIGURED_NAMESPACE);
        }

        @Test
        void aLeadingDotIsNotTreatedAsAnEmptyServiceName() {
            DiscoverInstancesRequest request = resolve(String.format(LEADING_DOT, CATALOG));

            assertThat(request.serviceName()).isEqualTo(String.format(LEADING_DOT, CATALOG));
            assertThat(request.namespaceName()).isEqualTo(CONFIGURED_NAMESPACE);
        }
    }

    @Nested
    class Ports {

        @Test
        void anInstanceGetsTheDefaultPortWhenItsServiceHasNoOverride() {
            List<ServiceInstance> instances =
                    EcsDiscoveryClient.getDefaultServiceInstances(discovery, properties, CATALOG);

            assertThat(instances).singleElement().extracting(ServiceInstance::getPort).isEqualTo(DEFAULT_PORT);
        }

        @Test
        void aPerServicePortOverridesTheDefault() {
            properties.setServicePorts(Map.of(CATALOG, 9091));

            List<ServiceInstance> instances =
                    EcsDiscoveryClient.getDefaultServiceInstances(discovery, properties, CATALOG);

            assertThat(instances).singleElement().extracting(ServiceInstance::getPort).isEqualTo(9091);
        }

        @Test
        void thePerServiceOverrideIsKeyedByTheExtractedNameNotTheQualifiedId() {
            properties.setServicePorts(Map.of(CATALOG, 9091));

            List<ServiceInstance> instances = EcsDiscoveryClient.getDefaultServiceInstances(
                    discovery, properties, String.format(QUALIFIED, CATALOG, OTHER_NAMESPACE));

            assertThat(instances).singleElement().extracting(ServiceInstance::getPort).isEqualTo(9091);
        }
    }

    @Nested
    class Listing {

        @BeforeEach
        void listReturnsOneService() {
            when(discovery.listServices(any(ListServicesRequest.class))).thenReturn(ListServicesResponse.builder()
                    .services(ServiceSummary.builder().name(CATALOG).build()).build());
        }

        @Test
        void withoutANamespaceIdEveryServiceIsListed() {
            EcsDiscoveryClient.getEcsServices(discovery, properties);

            ArgumentCaptor<ListServicesRequest> request = ArgumentCaptor.forClass(ListServicesRequest.class);
            verify(discovery).listServices(request.capture());
            assertThat(request.getValue().filters()).isEmpty();
        }

        @Test
        void aConfiguredNamespaceIdBecomesAFilterSoOtherNamespacesAreNotReturned() {
            properties.setNamespaceId(NAMESPACE_ID);

            EcsDiscoveryClient.getEcsServices(discovery, properties);

            ArgumentCaptor<ListServicesRequest> request = ArgumentCaptor.forClass(ListServicesRequest.class);
            verify(discovery).listServices(request.capture());
            assertThat(request.getValue().filters()).singleElement()
                    .satisfies(filter -> assertThat(filter.values()).containsExactly(NAMESPACE_ID));
        }

        @Test
        void servicesConfiguredAsAlwaysPresentAreAppendedToWhatCloudMapReturns() {
            properties.setIncludeServices(List.of(ALWAYS_PRESENT));

            assertThat(EcsDiscoveryClient.getEcsServices(discovery, properties)).containsExactly(CATALOG, ALWAYS_PRESENT);
        }

        @Test
        void theResultIsMutableBecauseCallersAppendToIt() {
            List<String> services = EcsDiscoveryClient.getEcsServices(discovery, properties);

            assertThat(services).isInstanceOf(java.util.ArrayList.class);
        }
    }

    @Test
    void theClientDescribesItselfForTheActuatorEndpoint() {
        assertThat(new EcsDiscoveryClient(properties, discovery).description()).isEqualTo("ecs discovery client");
    }

    @Test
    void theInstanceMethodDelegatesToTheStaticResolution() {
        assertThat(new EcsDiscoveryClient(properties, discovery).getInstances(CATALOG)).hasSize(1);
    }
}
