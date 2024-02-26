package com.asrevo.cloud.local.discovery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ashraf
 * LocalDiscoveryClient to get services in ractive mode
 */
@Slf4j
public class LocalDiscoveryClient implements DiscoveryClient {
    private final LocalDiscoveryProperties properties;

    /**
     * @param properties for defining the LocalDiscoveryClient
     */
    public LocalDiscoveryClient(LocalDiscoveryProperties properties) {
        this.properties = properties;
    }

    @Override
    public String description() {
        return "local discovery client";
    }

    /**
     * @param serviceId The serviceId to query.
     * @return available services
     */

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        return properties.getServices().get(serviceId).stream().map(it -> new LocalServiceInstance(serviceId, it)).collect(Collectors.toList());
    }

    @Override
    public List<String> getServices() {
        return properties.getServices().keySet().stream().toList();
    }
}
