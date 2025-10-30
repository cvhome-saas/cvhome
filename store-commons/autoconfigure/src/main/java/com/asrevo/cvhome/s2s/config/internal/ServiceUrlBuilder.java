package com.asrevo.cvhome.s2s.config.internal;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.ServiceDomain;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

@Slf4j
public record ServiceUrlBuilder(ServiceDomainProperties serviceDomainProperties, Environment environment) {
	public String getServiceUrl(String serviceName) {
		ServiceDomain requestedService = serviceDomainProperties.getService(serviceName);
		ServiceDomain currentService = serviceDomainProperties
			.getService(environment.getProperty("spring.application.name"));
		if (requestedService.namespace().equals(currentService.namespace())) {
			log.info("will create internal client for {}", serviceName);
			return "lb://" + serviceName;
		}
		else {
			ServiceDomain gateway = serviceDomainProperties.getService(requestedService.gatewayServiceName());
			log.info("will create external client for {} using gateway {} in namespace {}", serviceName, gateway.name(),
					gateway.namespace());
			return "lb://" + gateway.name() + "." + gateway.namespace() + "/" + serviceName;
		}
	}

	public String getServiceUrl(Pod pod, String subService) {
		ServiceDomain requestedService = serviceDomainProperties.getService(subService);
		ServiceDomain gateway = serviceDomainProperties.getService(requestedService.gatewayServiceName());
		return switch (pod.endpoint().type()) {
			case INTERNAL -> "lb://" + gateway.name() + "." + pod.endpoint().endpoint() + "/" + subService;
			case EXTERNAL -> pod.endpoint().endpoint() + "/" + subService;
			case null -> pod.endpoint().endpoint() + "/" + subService;
		};
	}

	public String getServiceUrl(Pod pod) {
		return switch (pod.endpoint().type()) {
			case INTERNAL -> "lb://" + "store-pod-gateway" + "." + pod.endpoint().endpoint();
			case EXTERNAL -> pod.endpoint().endpoint();
			case null -> pod.endpoint().endpoint();
		};
	}
}
