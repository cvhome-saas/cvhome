package com.asrevo.cvhome.gateway.service.impl;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.manager.api.CachedRouterService;
import com.asrevo.cvhome.manager.api.RouterAllocationService;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.function.Function;

@Service
public class CachedRouterServiceImpl implements CachedRouterService {

	private final Function<ManagerStoreId, Mono<Pod>> podCache;

	public CachedRouterServiceImpl(RouterAllocationService routerAllocationService) {
		this.podCache = ofMono(Duration.ofMinutes(10), routerAllocationService::getStorePodByStoreId);
	}

	public static <T, R> Function<R, Mono<T>> ofMono(Duration duration, Function<R, Mono<T>> fn) {
		final AsyncLoadingCache<R, T> cache = Caffeine.newBuilder()
			.expireAfterWrite(duration.multipliedBy(2))
			.refreshAfterWrite(duration)
			.buildAsync((k, e) -> fn.apply(k).subscribeOn(Schedulers.fromExecutor(e)).toFuture());

		return (k) -> Mono.fromFuture(cache.get(k));
	}

	@Override
	public Mono<Pod> getStorePodByStoreId(ManagerStoreId store) {
		return podCache.apply(store);
	}

}
