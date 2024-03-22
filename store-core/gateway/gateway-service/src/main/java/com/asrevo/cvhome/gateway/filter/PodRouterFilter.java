package com.asrevo.cvhome.gateway.filter;

import com.asrevo.cvhome.commons.domain.DomainReference;
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

import java.net.URI;

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
        if (uri.getPath().startsWith("/store")) {
            UriComponents uriComponents = UriComponentsBuilder.fromUri(uri).build();
            String storeId = uriComponents.getQueryParams().getFirst("storeId");
            if (storeId != null) {
                Mono<ServerHttpRequest> httpRequest = getServerHttpRequest(exchange, uriComponents, new DomainReference(storeId));
                return httpRequest.flatMap(it -> {
                            exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, it.getURI());
                            return chain.filter(exchange.mutate().request(it).build());
                        });
//                        .onErrorResume(throwable -> chain.filter(exchange));
            }
        }
        return chain.filter(exchange);
    }

    @SneakyThrows
    private Mono<ServerHttpRequest> getServerHttpRequest(ServerWebExchange exchange, UriComponents uriComponents, DomainReference reference) {
        return this.router.getAllocation(reference)
                .map(dto -> buildUri(uriComponents, dto))
                .map(newUri -> exchange.getRequest().mutate().uri(newUri).build());
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}