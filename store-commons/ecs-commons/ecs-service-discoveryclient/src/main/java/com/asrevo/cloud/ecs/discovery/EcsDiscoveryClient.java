package com.asrevo.cloud.ecs.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryClient;
import software.amazon.awssdk.services.servicediscovery.model.DiscoverInstancesRequest;
import software.amazon.awssdk.services.servicediscovery.model.DiscoverInstancesResponse;
import software.amazon.awssdk.services.servicediscovery.model.HttpInstanceSummary;
import software.amazon.awssdk.services.servicediscovery.model.ListServicesRequest;
import software.amazon.awssdk.services.servicediscovery.model.ListServicesResponse;
import software.amazon.awssdk.services.servicediscovery.model.ServiceFilter;
import software.amazon.awssdk.services.servicediscovery.model.ServiceSummary;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EcsDiscoveryClient implements DiscoveryClient {

    private static final String NAMESPACE_SEPARATOR = ".";

    private final EcsDiscoveryProperties properties;

    private final ServiceDiscoveryClient discovery;

    public EcsDiscoveryClient(EcsDiscoveryProperties properties, ServiceDiscoveryClient discovery) {
        this.properties = properties;
        this.discovery = discovery;
    }

    public static List<ServiceInstance> getDefaultServiceInstances(ServiceDiscoveryClient discovery,
                                                                   EcsDiscoveryProperties properties, String serviceId) {
        log.info("getting services for {}", serviceId);
        String extractedServiceId = extractServiceName(serviceId).orElse(serviceId);
        String extractedNamespace = extractNamespace(serviceId).orElse(properties.getNamespace());
        DiscoverInstancesRequest request = DiscoverInstancesRequest.builder()
                .namespaceName(extractedNamespace)
                .serviceName(extractedServiceId)
                .build();
        DiscoverInstancesResponse discoverInstancesResponse = discovery.discoverInstances(request);
        List<HttpInstanceSummary> instances = discoverInstancesResponse.instances();
        log.info("getting {} services for {} in namespace {}", instances.size(), extractedServiceId,
                extractedNamespace);
        return instances.stream().map(instance -> {
            Integer defaultPort = properties.getServicePorts()
                    .getOrDefault(extractedServiceId, properties.getDefaultPort());
            return (ServiceInstance) new CloudMapServiceInstance(instance, defaultPort);
        }).toList();
    }

    private static Optional<String> extractNamespace(String serviceId) {
        int firstSplitter = serviceId.indexOf(NAMESPACE_SEPARATOR);
        if (firstSplitter > 0 && serviceId.length() > firstSplitter + 1) {
            return Optional.of(serviceId.substring(firstSplitter + 1));
        } else {
            return Optional.empty();
        }
    }

    private static Optional<String> extractServiceName(String serviceId) {
        int firstSplitter = serviceId.indexOf(NAMESPACE_SEPARATOR);
        if (firstSplitter > 0) {
            return Optional.of(serviceId.substring(0, firstSplitter));
        } else {
            return Optional.empty();
        }
    }

    public static List<String> getEcsServices(ServiceDiscoveryClient discovery, EcsDiscoveryProperties properties) {
        log.info("getting all services");
        List<ServiceFilter> filters = new ArrayList<>();
        if (properties.getNamespaceId() != null) {
            log.info("will apply namespace id filter for {}", properties.getNamespaceId());
            filters.add(ServiceFilter.builder().name("NAMESPACE_ID").values(properties.getNamespaceId()).build());
        }
        ListServicesRequest servicesRequest = ListServicesRequest.builder().filters(filters).build();
        ListServicesResponse listServicesResponse = discovery.listServices(servicesRequest);
        List<ServiceSummary> services = listServicesResponse.services();
        List<String> list = services.stream().map(ServiceSummary::name).toList();
        ArrayList<String> result = new ArrayList<>(list);
        result.addAll(properties.getIncludeServices());
        return result;
    }

    @Override
    public String description() {
        return "ecs discovery client";
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        return getDefaultServiceInstances(this.discovery, this.properties, serviceId);
    }

    @Override
    public List<String> getServices() {
        return getEcsServices(this.discovery, this.properties);
    }

}
