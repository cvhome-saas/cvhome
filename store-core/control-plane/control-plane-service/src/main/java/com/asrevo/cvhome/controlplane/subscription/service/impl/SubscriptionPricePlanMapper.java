package com.asrevo.cvhome.controlplane.subscription.service.impl;

import com.asrevo.cvhome.subscription.commons.PricePlanCost;
import com.asrevo.cvhome.subscription.commons.RecurringPlan;
import com.asrevo.cvhome.subscription.commons.SubscriptionPlanOption;
import com.asrevo.cvhome.subscription.commons.SubscriptionPlanTable;
import com.asrevo.cvhome.controlplane.subscription.domain.SubscriptionPricePlanEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface SubscriptionPricePlanMapper {

	@Mapping(source = "subscriptionPlan.limits", target = "limits")
	@Mapping(source = "subscriptionPlan.feature", target = "feature")
	@Mapping(source = "it", target = "previousCost", qualifiedByName = "toPreviousCost")
	SubscriptionPlanOption toOption(SubscriptionPricePlanEntity it);

	@Named("toPreviousCost")
	default PricePlanCost toPreviousCost(SubscriptionPricePlanEntity it) {
		if (RecurringPlan.YEAR.equals(it.getRecurringPlan())) {
			return PricePlanCost.from(it.getCost().currency(), it.getSubscriptionPlan(), it.getRecurringPlan());
		}
		else {
			return it.getCost();
		}
	}

	default SubscriptionPlanTable toTable(List<SubscriptionPricePlanEntity> list) {
		List<SubscriptionPlanOption> tableOptions = list.stream().map(this::toOption).toList();
		return new SubscriptionPlanTable(tableOptions);
	}

}
