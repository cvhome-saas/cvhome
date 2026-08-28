package com.asrevo.cvhome.gateway.routing;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

/**
 * Stands in for a pod's edge: the integration tests register a pod whose endpoint is this very server, so a request
 * proxied through {@code /spg/**} lands here and reports what the pod would have seen.
 */
@RestController
@RequestMapping("/echo")
public class EchoPodController {

    @RequestMapping("/**")
    public Map<String, String> echo(ServerWebExchange exchange) {
        var request = exchange.getRequest();
        return Map.of("method", String.valueOf(request.getMethod()),
                "path", request.getURI().getRawPath(),
                "query", String.valueOf(request.getURI().getRawQuery()),
                "host", String.valueOf(request.getHeaders().getFirst(HttpHeaders.HOST)));
    }

}
