package com.asrevo.cvhome.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;

import com.asrevo.cvhome.commons.domain.ServiceDomain;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import com.asrevo.cvhome.s2s.utils.RedirectionUrlBuilder;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Mono;

import static com.asrevo.cvhome.s2s.utils.RedirectionUrlBuilder.getPort;
import static com.asrevo.cvhome.s2s.utils.RedirectionUrlBuilder.getScheme;

@Controller
@RequestMapping("redirect")
@AllArgsConstructor
@Slf4j
public class RedirectionController {

    private final ServiceDomainProperties serviceDomainProperties;

    @GetMapping(value = "internal", params = {"serviceName"})
    public Mono<Void> redirect(@RequestParam() String serviceName,
                               @RequestParam(required = false, defaultValue = "") String path, ServerWebExchange exchange) {

        ServerHttpRequest request = exchange.getRequest();

        ServiceDomain serviceDomain = serviceDomainProperties.getService(serviceName);

        if (serviceDomain == null) {
            throw new RuntimeException(serviceName + " not defined");
        }

        String fullUrl = new RedirectionUrlBuilder(getScheme(request), getPort(request), serviceDomain)
                .getRedirectionUrl(path);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.PERMANENT_REDIRECT);
        response.getHeaders().add("Location", fullUrl);
        return response.setComplete();
    }

}
