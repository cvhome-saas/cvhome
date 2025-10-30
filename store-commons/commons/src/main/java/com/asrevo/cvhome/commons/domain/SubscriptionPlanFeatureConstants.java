package com.asrevo.cvhome.commons.domain;

import static com.asrevo.cvhome.commons.domain.SubscriptionPlanFeature.of;
import static com.asrevo.cvhome.commons.domain.SubscriptionPlanFeatureCode.*;

import java.util.List;

public class SubscriptionPlanFeatureConstants {

	public static final SubscriptionPlanFeatureList FREE_FEATURE = new SubscriptionPlanFeatureList(
			List.of(of(FREE_STORES), of(FREE_ORDERS), of(FREE_PRODUCTS), of(FREE_VISITORS), of(FREE_ACCOUNTS)));

	public static final SubscriptionPlanFeatureList LIMITED_FEATURE = new SubscriptionPlanFeatureList(List
		.of(of(LIMITED_STORES), of(LIMITED_ORDERS), of(LIMITED_PRODUCTS), of(LIMITED_VISITORS), of(LIMITED_ACCOUNTS)));

	public static final SubscriptionPlanFeatureList BASIC_FEATURE = new SubscriptionPlanFeatureList(
			List.of(of(BASIC_STORES), of(BASIC_ORDERS), of(BASIC_PRODUCTS), of(BASIC_VISITORS), of(BASIC_ACCOUNTS)));

	public static final SubscriptionPlanFeatureList PERFORMANCE_FEATURE = new SubscriptionPlanFeatureList(
			List.of(of(PERFORMANCE_STORES), of(PERFORMANCE_ORDERS), of(PERFORMANCE_PRODUCTS), of(PERFORMANCE_VISITORS),
					of(PERFORMANCE_ACCOUNTS)));

}
