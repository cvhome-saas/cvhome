package com.asrevo.cvhome.controlplane.subscription.service.impl;

import com.asrevo.cvhome.commons.domain.SubscriptionPlan;
import com.asrevo.cvhome.controlplane.subscription.commons.PricePlanCost;
import com.asrevo.cvhome.controlplane.subscription.commons.ProductId;
import com.asrevo.cvhome.controlplane.subscription.commons.RecurringPlan;

public record ProductPriceDetails(ProductId productId, SubscriptionPlan subscriptionPlan, RecurringPlan recurringPlan,
                                  PricePlanCost pricePlanCost) {
}
