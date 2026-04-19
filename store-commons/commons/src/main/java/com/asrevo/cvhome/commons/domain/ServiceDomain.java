package com.asrevo.cvhome.commons.domain;

public record ServiceDomain(String name, String domain, String port, String schema, String namespace,
                            String gatewayServiceName) {

    private static final String PATH_SEPARATOR = "/";

    private static final String COLON_SEPARATOR = ":";

    private static final String PROTOCOL_SEPARATOR = "://";

    public String getServiceHost() {
        return schema() + PROTOCOL_SEPARATOR + domain() + COLON_SEPARATOR + port();
    }

    public String getServiceHost(String service) {
        return schema() + PROTOCOL_SEPARATOR + domain() + COLON_SEPARATOR + port() + PATH_SEPARATOR + service
                + PATH_SEPARATOR;
    }
}
