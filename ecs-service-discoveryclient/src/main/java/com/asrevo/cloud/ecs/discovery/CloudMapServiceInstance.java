package com.asrevo.cloud.ecs.discovery;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import software.amazon.awssdk.services.servicediscovery.model.HttpInstanceSummary;

import java.net.URI;
import java.util.Map;

public class CloudMapServiceInstance implements ServiceInstance {
    private final HttpInstanceSummary instance;
    private final Integer port;

    public CloudMapServiceInstance(HttpInstanceSummary instance, Integer port) {
        this.instance = instance;
        this.port = port;
    }


    @Override
    public String getServiceId() {
        return this.instance.serviceName();
    }

    public String getNamespace() {
        return this.instance.namespaceName();
    }

    @Override
    public String getHost() {
        return instance.attributes().get("AWS_INSTANCE_IPV4");
    }

    @Override
    public int getPort() {
        return Integer.parseInt(instance.attributes().getOrDefault("AWS_INSTANCE_PORT", port.toString()));
    }

    @Override
    public boolean isSecure() {
        return Boolean.parseBoolean(instance.attributes().getOrDefault("SECURE", "false"));
    }

    @Override
    public URI getUri() {
        return DefaultServiceInstance.getUri(this);
    }

    @Override
    public Map<String, String> getMetadata() {
        return instance.attributes();
    }

    @Override
    public String getInstanceId() {
        return instance.instanceId();
    }

    @Override
    public String getScheme() {
        return isSecure() ? "https" : "http";
    }
}
