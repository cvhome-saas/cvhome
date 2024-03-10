package com.asrevo.cvhome.wallet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties("com.asrevo.cvhome")
public record ServiceDomainProperties(Map<String, ServiceDomain> services) {
}

record ServiceDomain(String name, String domain, String port, String schema, String namespace) {

}
