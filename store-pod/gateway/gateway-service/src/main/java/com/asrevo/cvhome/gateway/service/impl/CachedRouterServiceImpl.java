package com.asrevo.cvhome.gateway.service.impl;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.gateway.service.CachedRouterService;
import com.asrevo.cvhome.s2s.clients.RouterAllocationService;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.function.Function;

@Service
public class CachedRouterServiceImpl implements CachedRouterService {
    private final Function<Domain, Mono<ManagerStoreId>> cache;

    public CachedRouterServiceImpl(RouterAllocationService routerAllocationService) {
        this.cache = ofMono(Duration.ofMinutes(10), routerAllocationService::getAllocation);
    }

    public static <T> Function<Domain, Mono<T>> ofMono(Duration duration, Function<Domain, Mono<T>> fn) {
        final AsyncLoadingCache<Domain, T> cache = Caffeine.newBuilder()
                .expireAfterWrite(duration.multipliedBy(2))
                .refreshAfterWrite(duration)
                .buildAsync((k, e) ->
                        fn.apply(k)
                                .subscribeOn(Schedulers.fromExecutor(e))
                                .toFuture());

        return (k) -> Mono.fromFuture(cache.get(k));
    }

    @Override
    public Mono<ManagerStoreId> getAllocation(Domain domain) {
        return cache.apply(domain);
    }
}
