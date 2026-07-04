package com.asrevo.cvhome.s2s.config.internal;

import org.springframework.core.env.Environment;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.ServiceDomain;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public record ServiceUrlBuilder(ServiceDomainProperties serviceDomainProperties, Environment environment) {
    private static final String LB_PREFIX = "lb://";

    public String getServiceUrl(String serviceName) {
        ServiceDomain requestedService = serviceDomainProperties.getService(serviceName);
        ServiceDomain currentService = serviceDomainProperties
                .getService(environment.getProperty("spring.application.name"));
        if (requestedService.namespace().equals(currentService.namespace())) {
            log.info("will create internal client for {}", serviceName);
            return LB_PREFIX + serviceName;
        } else {
            ServiceDomain gateway = serviceDomainProperties.getService(requestedService.gatewayServiceName());
            log.info("will create external client for {} using gateway {} in namespace {}", serviceName, gateway.name(),
                    gateway.namespace());
            return LB_PREFIX + gateway.name() + "." + gateway.namespace() + "/" + serviceName;
        }
    }

    public String getServiceUrl(Pod pod) {
        return switch (pod.endpoint().type()) {
            case INTERNAL -> LB_PREFIX + "spg." + pod.endpoint().endpoint();
            case EXTERNAL -> pod.endpoint().endpoint();
            case null -> pod.endpoint().endpoint();
        };
    }

    public String getServiceUrl(Pod pod, String sub) {
        return getServiceUrl(pod) + "/" + sub;
    }
}
