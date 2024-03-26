package com.asrevo.cvhome.gateway.filters;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.dto.PodReferenceDto;
import com.asrevo.cvhome.s2s.clients.RouterAllocationService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Optional;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

@Component
@Slf4j
public class PodRouterFilter implements GlobalFilter, Ordered {
    private final RouterAllocationService router;

    public PodRouterFilter(RouterAllocationService router) {
        this.router = router;
    }

    @SneakyThrows
    private static URI buildUri(UriComponentsBuilder uriComponents, PodReferenceDto dto) {
        uriComponents.queryParam("storeId", dto.reference());
        URI location = new URI(dto.location());
        uriComponents.host(location.getHost());
        uriComponents.scheme(location.getScheme());
        uriComponents.port(location.getPort());
        URI uri = uriComponents.build().toUri();
        log.info("will build uri for {} , {} to {}", dto.reference(), dto.location(), uri);
        return uri;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI uri = exchange.getRequest().getURI();
        String hostName = Optional.ofNullable(exchange.getRequest().getHeaders().getHost()).map(InetSocketAddress::getHostName).orElse(null);
        if (hostName != null) {
            Mono<ServerHttpRequest> httpRequest = getServerHttpRequest(exchange, UriComponentsBuilder.fromUri(uri), new Domain(hostName));
            return httpRequest.flatMap(it -> {
                log.info("will route {} to {}", uri, it.getURI());
                exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, it.getURI());
                return chain.filter(exchange.mutate().request(it).build());
            });
//                    .onErrorResume(throwable -> chain.filter(exchange));
        }

        return chain.filter(exchange);
    }

    @SneakyThrows
    private Mono<ServerHttpRequest> getServerHttpRequest(ServerWebExchange exchange, UriComponentsBuilder uriComponents, Domain domain) {
        return this.router.getAllocation(domain)
                .map(dto -> buildUri(uriComponents, dto))
                .map(newUri -> exchange.getRequest()
                        .mutate()
                        .uri(newUri)
                        .header("host", newUri.getHost())
                        .build());
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}