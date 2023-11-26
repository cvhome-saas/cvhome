package com.asrevo.cloud.ecs.discovery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryAsyncClient;
import software.amazon.awssdk.services.servicediscovery.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class EcsReactiveDiscoveryClient implements ReactiveDiscoveryClient {
    private final EcsDiscoveryProperties properties;
    private final ServiceDiscoveryAsyncClient discoveryAsync;

    public EcsReactiveDiscoveryClient(EcsDiscoveryProperties properties, ServiceDiscoveryAsyncClient discoveryAsync) {
        this.properties = properties;
        this.discoveryAsync = discoveryAsync;
    }

    @Override
    public String description() {
        return "ecs reactive discovery client";
    }

    @Override
    public Flux<ServiceInstance> getInstances(String serviceId) {
        return getDefaultServiceInstances(this.discoveryAsync, this.properties, serviceId);
    }

    public static Flux<ServiceInstance> getDefaultServiceInstances(ServiceDiscoveryAsyncClient discoveryAsync,
                                                                   EcsDiscoveryProperties properties,
                                                                   String serviceId) {
        DiscoverInstancesRequest request = DiscoverInstancesRequest.builder()
                .namespaceName(properties.getNamespace())
                .serviceName(serviceId)
                .build();
        return Mono.fromFuture(discoveryAsync.discoverInstances(request))
                .map(DiscoverInstancesResponse::instances)
                .doOnEach(it -> {
                    if (it.hasValue()) {
                        log.info("getting " + it.get().size() + " services for " + serviceId);
                    }
                })
                .flatMapMany(Flux::fromIterable)
                .map(CloudMapServiceInstance::new);
    }

    @Override
    public Flux<String> getServices() {
        return getEcsServices(this.discoveryAsync, this.properties);
    }

    public static Flux<String> getEcsServices(ServiceDiscoveryAsyncClient discoveryAsync, EcsDiscoveryProperties properties) {
        log.info("getting all services");
        List<ServiceFilter> filters = new ArrayList<>();
        if (properties.getNamespaceId() != null) {
            log.info("will apply namespace id filter for {}", properties.getNamespaceId());
            filters.add(ServiceFilter.builder().name("NAMESPACE_ID").values(properties.getNamespaceId()).build());
        }
        ListServicesRequest servicesRequest = ListServicesRequest.builder().filters(filters).build();
        CompletableFuture<ListServicesResponse> future = discoveryAsync.listServices(servicesRequest);
        return Mono.fromFuture(future)
                .map(ListServicesResponse::services)
                .flatMapMany(Flux::fromIterable)
                .map(ServiceSummary::name);
    }
}

