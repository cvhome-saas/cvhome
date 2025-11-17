package com.asrevo.cvhome.controlplane.subscription.service.impl;

import com.asrevo.cvhome.subscription.commons.SubscriptionPlanDetails;
import com.asrevo.cvhome.controlplane.subscription.domain.SubscriptionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

	SubscriptionPlanDetails toSubscriptionPlanDetails(SubscriptionEntity subscription);

}
