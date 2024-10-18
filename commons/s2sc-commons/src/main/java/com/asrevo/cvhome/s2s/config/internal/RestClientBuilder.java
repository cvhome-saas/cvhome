package com.asrevo.cvhome.s2s.config.internal;

import static com.asrevo.cvhome.s2s.utils.WebClientsUtils.build;

import com.asrevo.cvhome.s2s.model.ServiceDomain;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestClient;

public class RestClientBuilder {
    private final Environment environment;
    private final RestClient.Builder defaultMicroServiceBuilder;
    private final ServiceDomainProperties serviceDomainProperties;

    public RestClientBuilder(
            Environment environment,
            RestClient.Builder defaultMicroServiceBuilder,
            ServiceDomainProperties serviceDomainProperties) {
        this.environment = environment;
        this.defaultMicroServiceBuilder = defaultMicroServiceBuilder;
        this.serviceDomainProperties = serviceDomainProperties;
    }

    public <T> T buildClient(String serviceName, Class<T> tClass) {
        if (this.environment.matchesProfiles("lcl")) {
            return buildInternalClient(serviceName, tClass);
        } else {

            ServiceDomain requestedService = serviceDomainProperties.services().get(serviceName);
            ServiceDomain currentService =
                    serviceDomainProperties
                            .services()
                            .get(environment.getProperty("spring.application.name"));

            if (requestedService.gatewayServiceName().equals(currentService.gatewayServiceName())) {
                return buildInternalClient(serviceName, tClass);
            } else {
                return buildInternalClient(
                        serviceName + "." + requestedService.namespace(), tClass);
            }
        }
    }

    private <T> T buildInternalClient(String serviceName, Class<T> tClass) {
        return build(defaultMicroServiceBuilder, "lb://" + serviceName, tClass);
    }
}
