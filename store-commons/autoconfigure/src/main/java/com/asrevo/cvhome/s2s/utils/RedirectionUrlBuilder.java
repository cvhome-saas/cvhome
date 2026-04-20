package com.asrevo.cvhome.s2s.utils;

import java.util.Optional;

import org.springframework.http.server.reactive.ServerHttpRequest;

import com.asrevo.cvhome.commons.domain.ServiceDomain;

public record RedirectionUrlBuilder(String schema, Integer port, ServiceDomain serviceDomain) {

    public static final String SCHEMA_HEADER_KEY = "X-Forwarded-Proto";

    public static final String PORT_HEADER_KEY = "X-Forwarded-Port";

    private static final String SCHEMA_SPLITTER = "://";

    private static final String URL_SPLITTER = "/";

    public static Optional<String> getHeaderValue(ServerHttpRequest request, String headerKey) {
        if (request == null || headerKey == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(request.getHeaders().getFirst(headerKey))
                .map(String::trim)
                .filter(value -> !value.isEmpty());
    }

    public static Integer getPort(ServerHttpRequest request) {
        return getHeaderValue(request, PORT_HEADER_KEY).map(Integer::parseInt).orElse(request.getURI().getPort());
    }

    public static String getScheme(ServerHttpRequest request) {
        return getHeaderValue(request, SCHEMA_HEADER_KEY).orElse(request.getURI().getScheme());
    }

    public String getRedirectionUrl(String path) {

        String fullUrl = schema + SCHEMA_SPLITTER + serviceDomain.domain() + ":" + port;
        if (!path.isEmpty() && !path.equals(URL_SPLITTER)) {
            if (path.startsWith(URL_SPLITTER)) {
                fullUrl = fullUrl + path;
            } else {
                fullUrl = fullUrl + URL_SPLITTER + path;
            }
        }
        return fullUrl;
    }
}
