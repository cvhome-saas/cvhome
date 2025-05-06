package com.asrevo.cvhome.s2s.config.internal;

import static com.asrevo.cvhome.s2s.config.internal.WebClientBuilder.getServiceUrl;
import static com.asrevo.cvhome.s2s.utils.WebClientsUtils.build;

import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestClient;

@Slf4j
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
        String url = getServiceUrl(serviceDomainProperties, environment, serviceName);
        return build(defaultMicroServiceBuilder, url, tClass);
    }
}
