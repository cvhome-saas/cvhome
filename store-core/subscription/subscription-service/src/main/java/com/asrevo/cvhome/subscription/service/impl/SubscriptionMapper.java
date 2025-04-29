package com.asrevo.cvhome.subscription.service.impl;

import com.asrevo.cvhome.subscription.commons.SubscriptionPlanDetails;
import com.asrevo.cvhome.subscription.domain.SubscriptionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {
    SubscriptionPlanDetails toSubscriptionPlanDetails(SubscriptionEntity subscription);
}
