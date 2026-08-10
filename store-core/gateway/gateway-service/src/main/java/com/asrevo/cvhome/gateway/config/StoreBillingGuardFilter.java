package com.asrevo.cvhome.gateway.config;

import java.nio.charset.StandardCharsets;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.asrevo.cvhome.gateway.client.StoreBillingStatusClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Mono;

/**
 * Turns away seller traffic for a store whose subscription has lapsed.
 *
 * <p>
 * Applies only to pod traffic addressed by the {@code store} query parameter — the seller console's path through the
 * gateway. A shopper reaches a storefront by host, through the pod's own edge, and never crosses this filter: a
 * suspended store keeps selling, deliberately, because a merchant who cannot trade cannot earn the money to settle
 * the invoice.
 * </p>
 *
 * <p>
 * Answers {@code 402 Payment Required}, which is the one status that says precisely what is wrong. A 403 would send
 * the seller to their permissions, and a 404 would suggest the store is gone.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StoreBillingGuardFilter implements GlobalFilter, Ordered {

    /**
     * The prefix that carries seller traffic to a pod. Storefront traffic does not come this way.
     */
    private static final String POD_PREFIX = "/spg/";

    private static final String STORE_PARAM = "store";

    private static final String BODY = """
            {"code":"BILLING.STORE.SUSPENDED","category":"PAYMENT_REQUIRED",\
            "title":"BILLING.STORE.SUSPENDED",\
            "detail":"This store's subscription is not active. Renew it to continue."}""";

    private final StoreBillingStatusClient billingStatusClient;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (!path.startsWith(POD_PREFIX)) {
            return chain.filter(exchange);
        }
        String store = exchange.getRequest().getQueryParams().getFirst(STORE_PARAM);
        if (!billingStatusClient.blocked(store)) {
            return chain.filter(exchange);
        }
        log.info("Refusing {} for store {}: its subscription is not active", path, store);
        return refuse(exchange);
    }

    /**
     * Ahead of routing, so a refused request never reaches a pod. Behind the security filters, which run on their own
     * chain — this only decides whether an already-authenticated seller may act, not who they are.
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }

    private Mono<Void> refuse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.PAYMENT_REQUIRED);
        response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        DataBuffer buffer = response.bufferFactory().wrap(BODY.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

}
