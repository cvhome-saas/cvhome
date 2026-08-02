package com.asrevo.cvhome.s2s.config.internal;

import org.springframework.core.env.Environment;
import org.springframework.web.reactive.function.client.WebClient;

import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;

import lombok.extern.slf4j.Slf4j;

import static com.asrevo.cvhome.s2s.utils.WebClientsUtils.build;

@Slf4j
public class WebClientBuilder {

    private final Environment environment;

    private final WebClient.Builder defaultMicroServiceBuilder;

    private final ServiceDomainProperties serviceDomainProperties;

    public WebClientBuilder(Environment environment, WebClient.Builder defaultMicroServiceBuilder,
                            ServiceDomainProperties serviceDomainProperties) {
        this.environment = environment;
        this.defaultMicroServiceBuilder = defaultMicroServiceBuilder;
        this.serviceDomainProperties = serviceDomainProperties;
    }

    /**
     * @param errors the called API's error contract, so its failures arrive as the types it names — usually a constant
     *               from its {@code -external-api} module. Pass {@code null} for an API that names none
     */
    public <T> T buildClient(String serviceName, Class<T> tClass, RemoteErrorCatalog errors) {
        String url = new ServiceUrlBuilder(serviceDomainProperties, environment).getServiceUrl(serviceName);
        return build(defaultMicroServiceBuilder, url, tClass, errors);
    }

}
