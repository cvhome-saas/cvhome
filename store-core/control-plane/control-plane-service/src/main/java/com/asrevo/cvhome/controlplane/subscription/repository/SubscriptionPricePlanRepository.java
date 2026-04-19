package com.asrevo.cvhome.controlplane.subscription.repository;

import org.springframework.data.repository.CrudRepository;

import com.asrevo.cvhome.controlplane.subscription.commons.PriceId;
import com.asrevo.cvhome.controlplane.subscription.domain.SubscriptionPricePlanEntity;

public interface SubscriptionPricePlanRepository extends CrudRepository<SubscriptionPricePlanEntity, PriceId> {

}
