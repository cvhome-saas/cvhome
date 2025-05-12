package com.asrevo.cvhome.subscription.service.impl;

import com.asrevo.cvhome.commons.domain.SubscriptionPlan;
import com.asrevo.cvhome.subscription.commons.PricePlanCost;
import com.asrevo.cvhome.subscription.commons.ProductId;
import com.asrevo.cvhome.subscription.commons.RecurringPlan;

public record ProductPriceDetails(ProductId productId, SubscriptionPlan subscriptionPlan, RecurringPlan recurringPlan, PricePlanCost pricePlanCost){
}
