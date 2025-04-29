package com.asrevo.cvhome.subscription.service;

import com.asrevo.cvhome.commons.domain.SubscriptionPlan;
import com.asrevo.cvhome.subscription.commons.PriceId;
import com.asrevo.cvhome.subscription.commons.PricePlanCost;
import com.asrevo.cvhome.subscription.commons.ProductId;
import com.asrevo.cvhome.subscription.commons.RecurringPlan;

public interface StripeInitService {
    ProductId createProduct(SubscriptionPlan plan);

    PriceId createPrice(ProductId productId, SubscriptionPlan subscriptionPlan, RecurringPlan recurringPlan, PricePlanCost pricePlanCost);

    boolean exist(PriceId priceId);
}
