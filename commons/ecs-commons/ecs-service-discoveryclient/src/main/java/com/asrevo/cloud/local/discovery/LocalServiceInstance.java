package com.asrevo.cloud.local.discovery;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;

import java.net.URI;
import java.util.Map;

/**
 * @author ashraf
 * @see ServiceInstance
 * convert localInstance to ServiceInstance
 */
public class LocalServiceInstance implements ServiceInstance {
    private final LocalInstance instance;
    private final String serviceId;

    /**
     * @param serviceId  serviceId.
     * @param instance instance
     */
    public LocalServiceInstance(String serviceId, LocalInstance instance) {
        this.instance = instance;
        this.serviceId = serviceId;
    }


    @Override
    public String getServiceId() {
        return this.serviceId;
    }

    @Override
    public String getHost() {
        return instance.getUrl().getHost();
    }

    @Override
    public int getPort() {
        return instance.getUrl().getPort();
    }

    @Override
    public boolean isSecure() {
        return this.instance.getUrl().getProtocol().equals("https");
    }

    @Override
    public URI getUri() {
        return DefaultServiceInstance.getUri(this);
    }

    @Override
    public Map<String, String> getMetadata() {
        return instance.getMetadata();
    }

    @Override
    public String getScheme() {
        return getUri().getScheme();
    }
}
