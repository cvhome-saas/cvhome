package com.asrevo.cvhome.controlplane.subscription.service;

import java.util.Optional;

import com.asrevo.cvhome.controlplane.subscription.commons.PriceId;
import com.asrevo.cvhome.controlplane.subscription.commons.SubscriptionPlanOption;
import com.asrevo.cvhome.controlplane.subscription.commons.SubscriptionPlanTables;

public interface SubscriptionPlanTablesService {

    SubscriptionPlanTables tables();

    Optional<SubscriptionPlanOption> getSubscriptionPlanOption(PriceId priceId);

}
