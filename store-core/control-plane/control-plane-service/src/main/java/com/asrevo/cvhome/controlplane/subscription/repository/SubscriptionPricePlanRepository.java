package com.asrevo.cvhome.controlplane.subscription.repository;

import com.asrevo.cvhome.subscription.commons.PriceId;
import com.asrevo.cvhome.controlplane.subscription.domain.SubscriptionPricePlanEntity;
import org.springframework.data.repository.CrudRepository;

public interface SubscriptionPricePlanRepository extends CrudRepository<SubscriptionPricePlanEntity, PriceId> {

}
