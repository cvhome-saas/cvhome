package com.asrevo.cvhome.gateway.controller;

import com.asrevo.cvhome.s2s.model.ServiceDomain;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("redirect")
@AllArgsConstructor
public class RedirectionController {
    private final ServiceDomainProperties serviceDomainProperties;
    private final static int HTTP_PORT = 80;
    private final static int HTTPS_PORT = 443;
    private final static String SCHEMA_SPLITTER = "://";
    private final static String URL_SPLITTER = "/";

    @GetMapping(value = "internal", params = {"serviceName"})
    public Mono<Void> redirect(
            @RequestParam() String serviceName,
            @RequestParam(required = false, defaultValue = "") String path,
            ServerWebExchange exchange) {

        ServerHttpRequest request = exchange.getRequest();

        ServiceDomain serviceDomain = serviceDomainProperties.services().get(serviceName);

        if (serviceDomain == null) {
            throw new RuntimeException(serviceName + " not defined");
        }

        String fullUrl = buildRedirectionUrl(path, request, serviceDomain);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.PERMANENT_REDIRECT);
        response.getHeaders().add("Location", fullUrl);
        return response.setComplete();
    }

    private static String buildRedirectionUrl(String path, ServerHttpRequest request, ServiceDomain serviceDomain) {
        String scheme = request.getURI().getScheme();
        int port = request.getURI().getPort();
        boolean ignorePort = port == HTTP_PORT || port == HTTPS_PORT;

        String fullUrl;

        if (ignorePort) {
            fullUrl = scheme + SCHEMA_SPLITTER + serviceDomain.domain();
        } else {
            fullUrl = scheme + SCHEMA_SPLITTER + serviceDomain.domain() + ":" + port;
        }
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
