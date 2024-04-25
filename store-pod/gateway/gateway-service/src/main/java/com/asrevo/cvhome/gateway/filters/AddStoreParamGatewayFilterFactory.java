package com.asrevo.cvhome.gateway.filters;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.dto.PodReferenceDto;
import com.asrevo.cvhome.commons.utils.Constants;
import com.asrevo.cvhome.s2s.clients.RouterAllocationService;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
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

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_STORE;
import static com.asrevo.cvhome.s2s.utils.StoreCallUtils.getPodReference;

@Component
@Slf4j
public class AddStoreParamGatewayFilterFactory extends AbstractGatewayFilterFactory<AddStoreParamGatewayFilterFactory.Config> {
    public static final String TEMPLATE_KEY = "template";
    private final RouterAllocationService router;
    private final ServiceDomainProperties serviceDomainProperties;
    private static final String STORE_ID_PARAM = "store";
    private static final String STORE_ID_HEADER = "store";
    private static final String STORE_ID_COOKIE = "store";


    public AddStoreParamGatewayFilterFactory(RouterAllocationService router, ServiceDomainProperties serviceDomainProperties) {
        super(AddStoreParamGatewayFilterFactory.Config.class);
        this.router = router;
        this.serviceDomainProperties = serviceDomainProperties;
    }

    public List<String> shortcutFieldOrder() {
        return List.of(TEMPLATE_KEY);
    }

    @Override
    public GatewayFilter apply(AddStoreParamGatewayFilterFactory.Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            String store = getStore(request);

            if (store == null) {
                String hostName = Optional.ofNullable(request.getHeaders().getHost()).map(InetSocketAddress::getHostName).orElse(null);
                if (canExtractStoreFromHost(hostName)) {

                    return mapHostToStoreParam(hostName)
                            .flatMap(it -> {
                                ServerHttpRequest newRequest = addStoreParamsForRequest(request, it);
                                ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
                                return chain.filter(newExchange)
                                        .then(Mono.fromRunnable(() -> addStoreParamsForResponse(exchange.getRequest(), exchange.getResponse(), it)));
                            });
                }

            } else {
                ServerHttpRequest newRequest = addStoreParamsForRequest(request, store);
                ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
                return chain.filter(newExchange)
                        .then(Mono.fromRunnable(() -> addStoreParamsForResponse(exchange.getRequest(), exchange.getResponse(), store)));

            }
            return chain.filter(exchange);
        };
    }

    private void addStoreParamsForResponse(ServerHttpRequest request, ServerHttpResponse response, String store) {
        if (!response.isCommitted()) {
            addResponseHeader(response, store);
            addResponseCookie(response, store);
        }
    }

    private static void addResponseHeader(ServerHttpResponse response, String store) {
        response.getHeaders().add(STORE_ID_HEADER, store);
    }

    private static void addResponseCookie(ServerHttpResponse response, String store) {
        response.getHeaders().set("Set-Cookie", STORE_ID_HEADER + "=" + store + "; Path=/;");
    }

    private static ServerHttpRequest addStoreParamsForRequest(ServerHttpRequest request, String store) {
        ServerHttpRequest.Builder builder = request.mutate();
        if (extractStoreFromParams(request) == null) {
            addRequestParam(request, store, builder);
        }
        if (extractStoreFromHeaders(request) == null) {
            addRequestHeader(store, builder);
        }
        addRequestCookie(store, builder);
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

    private boolean canExtractStoreFromHost(String hostName) {
        return true;
    }

    private Mono<String> mapHostToStoreParam(String host) {
        Domain domain = new Domain(host);
        return Mono
                .defer(() ->
                        getPodReference(serviceDomainProperties, router.getAllocation(domain), this::buildFallbackPodReference))
                .map(PodReferenceDto::reference);
    }

    private PodReferenceDto buildFallbackPodReference() {
        return PodReferenceDto.from(null, DEFAULT_STORE);
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

    @Getter
    @Setter
    public static class Config {
        private String template;
    }
}
