package com.asrevo.cvhome.s2s.model;

import com.asrevo.cvhome.s2s.config.internal.CallStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Optional;

@ConfigurationProperties("com.asrevo.cvhome")
public record ServiceDomainProperties(Map<String, ServiceDomain> services, Map<String, CallStrategy> calls) {

    public CallStrategy getStrategy(String service, CallStrategy defaultStrategy) {
        Map<String, CallStrategy> calls = Optional.ofNullable(this.calls()).orElse(Map.of());
        return calls.getOrDefault(service, defaultStrategy);
    }

    public String getServiceHost(String service) {
        return this.services().get(service).getServiceHost();
    }

    public String getServiceHostThroughGateway(String service) {
        String storePodGateway = this.services().get(service).gatewayServiceName();
        return this.services().get(storePodGateway).getServiceHost(service);
    }
}

