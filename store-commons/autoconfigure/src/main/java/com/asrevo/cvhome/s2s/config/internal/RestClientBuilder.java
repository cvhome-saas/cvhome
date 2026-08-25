package com.asrevo.cvhome.s2s.config.internal;

import org.springframework.core.env.Environment;
import org.springframework.web.client.RestClient;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;

import lombok.extern.slf4j.Slf4j;

import static com.asrevo.cvhome.s2s.utils.WebClientsUtils.build;

@Slf4j
public class RestClientBuilder {

    private final Environment environment;

    private final RestClient.Builder defaultMicroServiceBuilder;
    private final RestClient.Builder defaultRestClientBuilder;

    private final ServiceDomainProperties serviceDomainProperties;

    public RestClientBuilder(Environment environment, RestClient.Builder defaultMicroServiceBuilder,
                             RestClient.Builder defaultRestClientBuilder,
                             ServiceDomainProperties serviceDomainProperties) {
        this.environment = environment;
        this.defaultMicroServiceBuilder = defaultMicroServiceBuilder;
        this.defaultRestClientBuilder = defaultRestClientBuilder;
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

    /**
     * @param errors the called API's error contract; may be {@code null} — see
     *               {@link #buildClient(String, Class, RemoteErrorCatalog)}
     */
    public <T> T buildClient(Pod pod, String serviceName, Class<T> tClass, RemoteErrorCatalog errors) {
        String url = new ServiceUrlBuilder(serviceDomainProperties, environment).getServiceUrl(pod, serviceName);
        return build(defaultRestClientBuilder, url, tClass, errors);
    }

}
