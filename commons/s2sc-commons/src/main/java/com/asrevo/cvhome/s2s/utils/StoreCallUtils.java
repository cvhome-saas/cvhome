package com.asrevo.cvhome.s2s.utils;

import com.asrevo.cvhome.s2s.config.internal.CallStrategy;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

public class StoreCallUtils {
    private static final String STORE_SERVICE_NAME = "store";
    private static final String ROUTER_SERVICE_NAME = "router";

    public static String getStoreUrl(ServiceDomainProperties properties) {
        final String storeUrl;

        CallStrategy storeCallStrategy = properties.getStrategy(STORE_SERVICE_NAME, CallStrategy.DEFAULT);

        if (storeCallStrategy == CallStrategy.DIRECT) {
            storeUrl = properties.getServiceHost(STORE_SERVICE_NAME);
        } else {
            storeUrl = properties.getServiceHostThroughGateway(STORE_SERVICE_NAME);
        }
        return storeUrl;
    }

    public static <T> Mono<T> getPodReference(ServiceDomainProperties properties, Mono<T> monoSupplier, Supplier<T> fallback) {
        CallStrategy routerCallStrategy = properties.getStrategy(ROUTER_SERVICE_NAME, CallStrategy.DEFAULT);
        if (routerCallStrategy == CallStrategy.IGNORE) {
            return Mono.fromSupplier(fallback);
        } else {
            return Mono.from(monoSupplier);
        }
    }

}
