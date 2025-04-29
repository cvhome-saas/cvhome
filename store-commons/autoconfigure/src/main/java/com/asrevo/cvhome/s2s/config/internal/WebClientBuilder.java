package com.asrevo.cvhome.s2s.config.internal;

import static com.asrevo.cvhome.s2s.utils.WebClientsUtils.build;

import com.asrevo.cvhome.commons.domain.ServiceDomain;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
public class WebClientBuilder {
    private final Environment environment;
    private final WebClient.Builder defaultMicroServiceBuilder;
    private final ServiceDomainProperties serviceDomainProperties;

    public WebClientBuilder(
            Environment environment,
            WebClient.Builder defaultMicroServiceBuilder,
            ServiceDomainProperties serviceDomainProperties) {
        this.environment = environment;
        this.defaultMicroServiceBuilder = defaultMicroServiceBuilder;
        this.serviceDomainProperties = serviceDomainProperties;
    }

    public <T> T buildClient(String serviceName, Class<T> tClass) {
        ServiceDomain requestedService = serviceDomainProperties.getService(serviceName);
        ServiceDomain currentService =
                serviceDomainProperties.getService(
                        environment.getProperty("spring.application.name"));

        if (requestedService.namespace().equals(currentService.namespace())) {
            log.info("will create internal client for {}", serviceName);
            return buildInternalClient(serviceName, tClass);
        } else {
            ServiceDomain gateway =
                    serviceDomainProperties.getService(requestedService.gatewayServiceName());
            log.info(
                    "will create external client for {} using gateway {} in namespace {}",
                    serviceName,
                    gateway.name(),
                    gateway.namespace());
            return buildInternalClient(
                    gateway.name() + "." + gateway.namespace() + "/" + serviceName, tClass);
        }
    }

    public <T> T buildInternalClient(String serviceName, Class<T> tClass) {
        return build(defaultMicroServiceBuilder, "lb://" + serviceName, tClass);
    }
}
