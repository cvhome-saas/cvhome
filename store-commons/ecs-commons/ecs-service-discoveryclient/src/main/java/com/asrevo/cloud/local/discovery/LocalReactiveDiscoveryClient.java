package com.asrevo.cloud.local.discovery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import reactor.core.publisher.Flux;

/**
 * @author ashraf
 * LocalReactiveDiscoveryClient to get services in ractive mode
 */
@Slf4j
public class LocalReactiveDiscoveryClient implements ReactiveDiscoveryClient {
    private final LocalDiscoveryProperties properties;

    /**
     * @param properties for defining the LocalReactiveDiscoveryClient
     */
    public LocalReactiveDiscoveryClient(LocalDiscoveryProperties properties) {
        this.properties = properties;
    }

    /**
     * @param properties for getting defined services
     * @param serviceId  The serviceId to query.
     * @return available services
     */
    public static Flux<ServiceInstance> getDefaultServiceInstances(
            LocalDiscoveryProperties properties, String serviceId) {

        return Flux.fromIterable(properties.getServices().get(serviceId))
                .map(it -> new LocalServiceInstance(serviceId, it));
    }

    @Override
    public String description() {
        return "local reactive discovery client";
    }

    /**
     * @param serviceId The serviceId to query.
     * @return available services
     */
    @Override
    public Flux<ServiceInstance> getInstances(String serviceId) {
        return getDefaultServiceInstances(this.properties, serviceId);
    }

    @Override
    public Flux<String> getServices() {
        return Flux.fromIterable(properties.getServices().keySet());
    }
}
