package com.asrevo.cvhome.manager.service.impl;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.subscription.api.CachedSubscriptionPlanDetailsService;
import com.asrevo.cvhome.subscription.api.SubscriptionPlanDetailsService;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class CachedSubscriptionPlanDetailsServiceImpl implements CachedSubscriptionPlanDetailsService {

	private final Function<ManagerOrgId, Mono<Object>> subscriptionPlanDetailsCache;

	public CachedSubscriptionPlanDetailsServiceImpl(SubscriptionPlanDetailsService subscriptionPlanDetailsService) {
		this.subscriptionPlanDetailsCache = ofMono(Duration.ofMinutes(10),
				subscriptionPlanDetailsService::subscriptionPlanDetails);
	}

	public static <T, R> Function<R, Mono<T>> ofMono(Duration duration, Function<R, Mono<T>> fn) {
		final AsyncLoadingCache<R, T> cache = Caffeine.newBuilder()
			.expireAfterWrite(duration.multipliedBy(2))
			.refreshAfterWrite(duration)
			.buildAsync((k, e) -> fn.apply(k).subscribeOn(Schedulers.fromExecutor(e)).toFuture());

		return (k) -> Mono.fromFuture(cache.get(k));
	}

	@Override
	public Mono<Object> subscriptionPlanDetails(ManagerOrgId orgId) {
		return subscriptionPlanDetailsCache.apply(orgId);
	}

}
