package com.asrevo.cvhome.gateway.filter;

import com.asrevo.cvhome.commons.domain.DomainReference;
import com.asrevo.cvhome.s2s.clients.RouterAllocationService;
import com.asrevo.cvhome.s2s.config.internal.CallStrategy;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import lombok.SneakyThrows;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

@Component
public class PodRouterFilter implements GlobalFilter, Ordered {
    private static final String STORE_SERVICE_PREFIX = "/store/";
    private static final String STORE_ID_PARAM = "store";
    private final RouterAllocationService router;
    private final String storeUri;
    private final CallStrategy callStrategy;

    public PodRouterFilter(RouterAllocationService router, GatewayProperties gatewayProperties, ServiceDomainProperties serviceDomainProperties) {
        this.router = router;
        Map<String, CallStrategy> calls = Optional.ofNullable(serviceDomainProperties.calls()).orElse(Map.of());
        if (CallStrategy.DIRECT.equals(calls.getOrDefault("store", CallStrategy.GATEWAY))) {
            this.storeUri = serviceDomainProperties.services().get("store").getServiceHost();
            this.callStrategy = CallStrategy.DIRECT;
        } else if (CallStrategy.GATEWAY.equals(calls.getOrDefault("store", CallStrategy.GATEWAY))) {
            this.storeUri = gatewayProperties.getRoutes().stream().filter(routeDefinition -> routeDefinition.getId().equals("store")).findFirst()
                    .map(it -> it.getUri().toString()).orElse(null);
            this.callStrategy = CallStrategy.GATEWAY;
        } else {
            throw new RuntimeException("Unsupported call strategy: " + calls.get("store"));
        }
    }

    @SneakyThrows
    private URI buildUri(UriComponents uriComponents, String location) {
        String path = uriComponents.getPath();
        if (CallStrategy.DIRECT.equals(callStrategy)) {
            if (path != null) {
                path = path.replaceFirst(STORE_SERVICE_PREFIX, "/");
            }
        }

        return new URI(location + path + "?" + uriComponents.getQuery());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI uri = exchange.getRequest().getURI();
        if (uri.getPath().startsWith(STORE_SERVICE_PREFIX)) {
            UriComponents uriComponents = UriComponentsBuilder.fromUri(uri).build();
            String storeId = uriComponents.getQueryParams().getFirst(STORE_ID_PARAM);
            if (storeId != null) {
            Mono<ServerHttpRequest> httpRequest = getServerHttpRequest(exchange, uriComponents, new DomainReference("storeId"));
            return httpRequest.flatMap(it -> {
                exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, it.getURI());
                return chain.filter(exchange.mutate().request(it).build());
            });
            }
        }
        return chain.filter(exchange);
    }

    @SneakyThrows
    private Mono<ServerHttpRequest> getServerHttpRequest(ServerWebExchange exchange, UriComponents uriComponents, DomainReference reference) {
        return Mono.defer(() ->
                        this.router.getAllocation(reference)
                                .map(dto -> buildUri(uriComponents, dto.location()))
                                .map(newUri -> exchange.getRequest().mutate().uri(newUri).build()))
                .onErrorResume(throwable -> {
                    if (throwable instanceof WebClientRequestException) {
                        if (storeUri != null) {
                            return Mono.just(buildUri(uriComponents, storeUri))
                                    .map(newUri -> exchange.getRequest().mutate().uri(newUri).build());
                        } else {
                            return Mono.error(throwable);
                        }
                    } else {
                        return Mono.error(throwable);
                    }
                });


    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}