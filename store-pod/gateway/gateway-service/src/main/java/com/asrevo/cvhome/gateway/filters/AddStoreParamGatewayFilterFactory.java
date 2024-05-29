package com.asrevo.cvhome.gateway.filters;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.gateway.service.CachedRouterService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Optional;


@Component
@Slf4j
public class AddStoreParamGatewayFilterFactory extends AbstractGatewayFilterFactory<AddStoreParamGatewayFilterFactory.Config> {
    public static final String TEMPLATE_KEY = "template";
    private static final String STORE_ID_PARAM = "store";
    private static final String STORE_ID_HEADER = "store";
    private static final String STORE_ID_COOKIE = "store";
    private final CachedRouterService router;


    public AddStoreParamGatewayFilterFactory(CachedRouterService router) {
        super(AddStoreParamGatewayFilterFactory.Config.class);
        this.router = router;
    }

    private static void addResponseHeader(ServerHttpResponse response, String store) {
        response.getHeaders().add(STORE_ID_HEADER, store);
    }

    private static void addResponseCookie(ServerHttpResponse response, String store) {
        response.getHeaders().set("Set-Cookie", STORE_ID_HEADER + "=" + store + "; Path=/;");
    }

    private static ServerHttpRequest addStoreParamsForRequest(Config config, ServerHttpRequest request, String store) {
        ServerHttpRequest.Builder builder = request.mutate();
        if (config.getAddRequestParam() && extractStoreFromParams(request) == null) {
            addRequestParam(request, store, builder);
        }
        if (config.getAddRequestHeader() && extractStoreFromHeaders(request) == null) {
            addRequestHeader(store, builder);
        }
        if (config.getAddRequestCookie()) {
            addRequestCookie(store, builder);
        }
        return builder.build();
    }

    private static void addRequestCookie(String store, ServerHttpRequest.Builder builder) {
        builder.headers(httpHeaders -> httpHeaders.set("Cookie", new HttpCookie(STORE_ID_COOKIE, store).toString()));
    }

    private static void addRequestHeader(String store, ServerHttpRequest.Builder builder) {
        builder.header(STORE_ID_HEADER, store);
    }

    private static void addRequestParam(ServerHttpRequest request, String store, ServerHttpRequest.Builder builder) {
        URI uri = request.getURI();
        StringBuilder query = new StringBuilder();
        String originalQuery = uri.getRawQuery();
        if (StringUtils.hasText(originalQuery)) {
            query.append(originalQuery);
            if (originalQuery.charAt(originalQuery.length() - 1) != '&') {
                query.append('&');
            }
        }
        query.append(STORE_ID_PARAM);
        query.append('=');
        query.append(store);

        URI newUri = UriComponentsBuilder.fromUri(uri).replaceQuery(query.toString()).build(true).toUri();
        builder.uri(newUri);
    }

    private static String getStore(ServerHttpRequest request) {
        String store = extractStoreFromParams(request);
        if (store != null) {
            return store;
        } else {
            return extractStoreFromHeaders(request);
        }
    }

    private static String extractStoreFromParams(ServerHttpRequest request) {
        String store = request.getQueryParams().getFirst(STORE_ID_PARAM);
        if (store != null && !store.trim().isEmpty()) {
            return store.trim();
        } else {
            return null;
        }
    }

    private static String extractStoreFromHeaders(ServerHttpRequest request) {
        String store = request.getHeaders().getFirst(STORE_ID_HEADER);
        if (store != null && !store.trim().isEmpty()) {
            return store.trim();
        } else {
            return null;
        }
    }

    private static String extractHostName(ServerHttpRequest request) {
        return Optional.ofNullable(request.getHeaders().getHost()).map(InetSocketAddress::getHostName).orElse(null);
    }

    @Override
    public GatewayFilter apply(AddStoreParamGatewayFilterFactory.Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String store = getStore(request);
            if (store == null) {
                String hostName = extractHostName(request);
                if (isValidHostName(hostName)) {
                    return mapHostToStoreParam(hostName)
                            .flatMap(it -> execute(config, exchange, chain, it));
                }
            } else {
                return execute(config, exchange, chain, store);
            }
            return chain.filter(exchange);
        };
    }

    public List<String> shortcutFieldOrder() {
        return List.of(TEMPLATE_KEY);
    }

    private Mono<Void> execute(Config config, ServerWebExchange exchange, GatewayFilterChain chain, String store) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest newRequest = addStoreParamsForRequest(config, request, store);
        ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
        return chain.filter(newExchange)
                .then(Mono.fromRunnable(() -> addStoreParamsForResponse(config, exchange, store)));
    }

    private void addStoreParamsForResponse(Config config, ServerWebExchange exchange, String store) {
        ServerHttpResponse response = exchange.getResponse();
        if (!response.isCommitted()) {
            if (config.getAddResponseHeader()) {
                addResponseHeader(response, store);
            }
            if (config.getAddResponseCookie()) {
                addResponseCookie(response, store);
            }
        }
    }

    private boolean isValidHostName(String hostName) {
        return true;
    }

    private Mono<String> mapHostToStoreParam(String host) {
        return router.getAllocation(new Domain(host)).map(it -> it.getId().toString());
    }

    @Getter
    @Setter
    public static class Config {
        private Boolean addRequestParam = false;
        private Boolean addRequestHeader = false;
        private Boolean addRequestCookie = false;
        private Boolean addResponseHeader = false;
        private Boolean addResponseCookie = false;
    }

}
