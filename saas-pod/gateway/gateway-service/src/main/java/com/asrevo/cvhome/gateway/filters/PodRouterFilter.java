package com.asrevo.cvhome.gateway.filters;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.dto.PodReferenceDto;
import com.asrevo.cvhome.s2s.clients.RouterAllocationService;
import lombok.SneakyThrows;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Optional;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

@Component
public class PodRouterFilter implements GlobalFilter, Ordered {
    private final RouterAllocationService router;

    public PodRouterFilter(RouterAllocationService router) {
        this.router = router;
    }

    @SneakyThrows
    private static URI buildUri(UriComponents uriComponents, PodReferenceDto dto) {
        return new URI(dto.location() + uriComponents.getPath() + "?" + uriComponents.getQuery());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI uri = exchange.getRequest().getURI();
        String hostName = Optional.ofNullable(exchange.getRequest().getHeaders().getHost()).map(InetSocketAddress::getHostName).orElse(null);
        if (hostName != null) {
            UriComponents uriComponents = UriComponentsBuilder.fromUri(uri).build();
            Mono<ServerHttpRequest> httpRequest = getServerHttpRequest(exchange, uriComponents, new Domain(hostName));
            return httpRequest.flatMap(it -> {
                        exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, it.getURI());
                        return chain.filter(exchange.mutate().request(it).build());
                    })
                    .onErrorResume(throwable -> chain.filter(exchange));
        }

        return chain.filter(exchange);
    }

    @SneakyThrows
    private Mono<ServerHttpRequest> getServerHttpRequest(ServerWebExchange exchange, UriComponents uriComponents, Domain domain) {
        return this.router.getAllocation(domain)
                .map(dto -> buildUri(uriComponents, dto))
                .map(newUri -> exchange.getRequest().mutate().uri(newUri).build());
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}