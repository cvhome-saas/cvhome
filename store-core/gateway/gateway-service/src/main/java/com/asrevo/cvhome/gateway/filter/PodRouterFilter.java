package com.asrevo.cvhome.gateway.filter;

import com.asrevo.cvhome.commons.domain.DomainReference;
import com.asrevo.cvhome.commons.dto.IpodDto;
import com.asrevo.cvhome.commons.dto.PodReferenceDto;
import com.asrevo.cvhome.s2s.clients.RouterAllocationService;
import com.asrevo.cvhome.s2s.config.internal.CallStrategy;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
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
import java.util.Map;
import java.util.Optional;

import static com.asrevo.cvhome.s2s.utils.StoreCallUtils.getPodReference;
import static com.asrevo.cvhome.s2s.utils.StoreCallUtils.getStoreUrl;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

@Component
public class PodRouterFilter implements GlobalFilter, Ordered {
    private static final String STORE_SERVICE_PREFIX = "/store/";
    private static final String STORE_ID_PARAM = "store";
    private static final String STORE_ID_HEADER = "store";
    private static final String STORE_SERVICE_NAME = "store";
    private static final String ROUTER_SERVICE_NAME = "router";
    private final RouterAllocationService router;
    private final CallStrategy storeCallStrategy;
    private final ServiceDomainProperties serviceDomainProperties;
    private final String fallbackStoreUrl;

    public PodRouterFilter(RouterAllocationService router, ServiceDomainProperties serviceDomainProperties) {
        this.router = router;
        Map<String, CallStrategy> calls = Optional.ofNullable(serviceDomainProperties.calls()).orElse(Map.of());
        this.storeCallStrategy = calls.getOrDefault(STORE_SERVICE_NAME, CallStrategy.DEFAULT);
        this.serviceDomainProperties = serviceDomainProperties;
        this.fallbackStoreUrl = getStoreUrl(serviceDomainProperties);
    }


    @SneakyThrows
    private URI buildUri(UriComponents uriComponents, IpodDto ipodDto) {
        String path = uriComponents.getPath();
        if (CallStrategy.DIRECT.equals(storeCallStrategy)) {
            if (path != null && path.startsWith(STORE_SERVICE_PREFIX)) {
                path = path.replaceFirst(STORE_SERVICE_PREFIX, "/");
            }
        }

        return new URI(ipodDto.location() + path + "?" + uriComponents.getQuery());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI uri = exchange.getRequest().getURI();
        if (uri.getPath().startsWith(STORE_SERVICE_PREFIX)) {
            UriComponents uriComponents = UriComponentsBuilder.fromUri(uri).build();
            String store = uriComponents.getQueryParams().getFirst(STORE_ID_PARAM);
            if (store != null) {
                Mono<ServerHttpRequest> httpRequest = getServerHttpRequest(exchange, uriComponents, new DomainReference(store));
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
        return Mono
                .defer(() -> getPodReference(serviceDomainProperties, router.getAllocation(reference), () -> buildFallbackPodReference(reference)))
                .map(it -> {
                    URI newUri = buildUri(uriComponents, it);
                    return exchange.getRequest().mutate()
                            .uri(newUri)
                            .header(STORE_ID_HEADER, reference.reference())
                            .build();
                });
    }

    private PodReferenceDto buildFallbackPodReference(DomainReference reference) {
        return PodReferenceDto.from(this.fallbackStoreUrl, reference.reference());
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}