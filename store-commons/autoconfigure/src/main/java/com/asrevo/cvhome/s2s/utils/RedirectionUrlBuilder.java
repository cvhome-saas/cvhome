package com.asrevo.cvhome.s2s.utils;

import java.util.List;
import java.util.Optional;

import org.springframework.http.server.reactive.ServerHttpRequest;

import com.asrevo.cvhome.commons.domain.ServiceDomain;

public record RedirectionUrlBuilder(String schema, Integer port, ServiceDomain serviceDomain) {

    public static final String SCHEMA_HEADER_KEY = "X-Forwarded-Proto";

    public static final String PORT_HEADER_KEY = "X-Forwarded-Port";

    private static final String SCHEMA_SPLITTER = "://";

    private static final String URL_SPLITTER = "/";

    public static Optional<String> getHeaderValue(ServerHttpRequest request, String headerKey) {
        return Optional.ofNullable(request.getHeaders().get(headerKey))
                .orElse(List.of())
                .stream()
                .map(it -> it.split(",")[0])
                .findFirst();
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
