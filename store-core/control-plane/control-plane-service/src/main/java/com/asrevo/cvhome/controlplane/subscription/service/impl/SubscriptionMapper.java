package com.asrevo.cvhome.controlplane.subscription.service.impl;

import org.mapstruct.Mapper;

import com.asrevo.cvhome.controlplane.subscription.commons.SubscriptionPlanDetails;
import com.asrevo.cvhome.controlplane.subscription.domain.SubscriptionEntity;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    SubscriptionPlanDetails toSubscriptionPlanDetails(SubscriptionEntity subscription);

}
