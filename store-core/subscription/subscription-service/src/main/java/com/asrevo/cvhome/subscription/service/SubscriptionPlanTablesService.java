package com.asrevo.cvhome.subscription.service;

import com.asrevo.cvhome.subscription.commons.PriceId;
import com.asrevo.cvhome.subscription.commons.SubscriptionPlanOption;
import com.asrevo.cvhome.subscription.commons.SubscriptionPlanTables;

import java.util.Optional;

public interface SubscriptionPlanTablesService {
    SubscriptionPlanTables tables();

    Optional<SubscriptionPlanOption> getSubscriptionPlanOption(PriceId priceId);
}
