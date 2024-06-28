package com.asrevo.cvhome.s2s.config.internal;

import com.asrevo.cvhome.s2s.model.ServiceDomain;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.function.client.WebClient;

import static com.asrevo.cvhome.s2s.utils.WebClientsUtils.build;

@Slf4j
public class WebClientBuilder {
    private final Environment environment;
    private final WebClient.Builder defaultMicroServiceBuilder;
    private final ServiceDomainProperties serviceDomainProperties;

    public WebClientBuilder(Environment environment,
                            WebClient.Builder defaultMicroServiceBuilder,
                            ServiceDomainProperties serviceDomainProperties) {
        this.environment = environment;
        this.defaultMicroServiceBuilder = defaultMicroServiceBuilder;
        this.serviceDomainProperties = serviceDomainProperties;
    }

    public <T> T buildClient(String serviceName, Class<T> tClass) {
        if (this.environment.matchesProfiles("lcl")) {
            log.info("will create internal client for {} in lcl env", serviceName);
            return buildInternalClient(serviceName, tClass);
        } else {

            ServiceDomain requestedService = serviceDomainProperties.services().get(serviceName);
            ServiceDomain currentService = serviceDomainProperties.services().get(environment.getProperty("spring.application.name"));

            if (requestedService.gatewayServiceName().equals(currentService.gatewayServiceName())) {
                log.info("will create internal client for {} in non-lcl env", serviceName);
                return buildInternalClient(serviceName, tClass);
            } else {
                log.info("will create internal client for {} in non-lcl env for namespace {}", serviceName, requestedService.namespace());
                return buildInternalClient(serviceName + "." + requestedService.namespace(), tClass);
            }
        }
    }

    private <T> T buildInternalClient(String serviceName, Class<T> tClass) {
        return build(defaultMicroServiceBuilder, "lb://" + serviceName, tClass);
    }
}
