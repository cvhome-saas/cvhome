package com.asrevo.cvhome.commons.domain;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record SubscriptionPlanLimitList(List<SubscriptionPlanLimitValue> limits) {
	public SubscriptionPlanLimitList {
		Set<SubscriptionPlanLimitKey> current = limits.stream()
			.map(SubscriptionPlanLimitValue::limitKey)
			.collect(Collectors.toSet());
		Set<SubscriptionPlanLimitKey> allLimits = Stream.of(SubscriptionPlanLimitKey.values())
			.collect(Collectors.toSet());

		if (current.size() != allLimits.size()) {
			throw new IllegalStateException("Limit list does not match all limits");
		}
	}

	public boolean exceeded(SubscriptionPlanLimitKey limitKey, Integer current) {
		return limits.stream()
			.filter(it -> it.limitKey().equals(limitKey))
			.findFirst()
			.map(it -> current > it.limit())
			.orElse(false);
	}
}
