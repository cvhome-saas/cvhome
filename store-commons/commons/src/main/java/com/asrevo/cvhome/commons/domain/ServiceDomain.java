package com.asrevo.cvhome.commons.domain;

public record ServiceDomain(String name, String domain, String port, String schema, String namespace,
                            String gatewayServiceName) {

    private static final String HOST_TEMPLATE = "%s://%s:%s";

    private static final String HOST_WITH_SERVICE_TEMPLATE = "%s://%s:%s/%s/";

    public String getServiceHost() {
        return HOST_TEMPLATE.formatted(schema(), domain(), port());
    }

    public String getServiceHost(String service) {
        return HOST_WITH_SERVICE_TEMPLATE.formatted(schema(), domain(), port(), service);
    }
}
