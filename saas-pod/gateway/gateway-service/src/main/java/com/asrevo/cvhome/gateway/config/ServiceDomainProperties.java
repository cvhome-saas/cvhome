package com.asrevo.cvhome.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties("com.asrevo.cvhome")
public record ServiceDomainProperties(Map<String, ServiceDomain> services) {
}

record ServiceDomain(String name, String domain, String port, String schema, String namespace) {
    public String getServiceHost() {
        return schema() + "://" + domain() + ":" + port();
    }

    public String getServiceHost(String service) {
        return schema() + "://" + domain() + ":" + port() + "/" + service;
    }
}
