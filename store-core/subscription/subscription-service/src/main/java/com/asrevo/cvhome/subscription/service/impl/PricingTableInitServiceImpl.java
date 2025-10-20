package com.asrevo.cvhome.subscription.service.impl;

import com.asrevo.cvhome.commons.domain.SubscriptionPlan;
import com.asrevo.cvhome.subscription.commons.PriceId;
import com.asrevo.cvhome.subscription.commons.PricePlanCost;
import com.asrevo.cvhome.subscription.commons.ProductId;
import com.asrevo.cvhome.subscription.commons.RecurringPlan;
import com.asrevo.cvhome.subscription.domain.SubscriptionPricePlanEntity;
import com.asrevo.cvhome.subscription.repository.SubscriptionPricePlanRepository;
import com.asrevo.cvhome.subscription.service.PricingTableInitService;
import com.asrevo.cvhome.subscription.service.StripeInitService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class PricingTableInitServiceImpl implements PricingTableInitService {
    private static final String CURRENCY = "usd";
    private final SubscriptionPricePlanRepository subscriptionPricePlanRepository;
    private final StripeInitService stripeInitService;

    @Transactional
    @Override
    public void init() {
        if (stripeInitService.isConfigured()) {
            List<ProductPriceDetails> productPriceDetails = stripeInitService.loadTable();
            int expectedPlans = getPaidSubscriptionPlans().size() * RecurringPlan.values().length;
            if (productPriceDetails.size() != expectedPlans
                    || subscriptionPricePlanRepository.count() != expectedPlans) {
                log.info("will create subscription plan table");
                createNewPricingTable();
            }
        }
    }

    private void createNewPricingTable() {
        List<SubscriptionPlan> subscriptionPlans = getPaidSubscriptionPlans();

        List<RecurringPlan> recurringPlans = Arrays.stream(RecurringPlan.values()).toList();

        Map<SubscriptionPlan, ProductId> productBySubscriptionPlan = new HashMap<>();

        List<SubscriptionPricePlanEntity> list =
                subscriptionPlans.stream()
                        .flatMap(
                                it ->
                                        recurringPlans.stream()
                                                .map(
                                                        rp -> {
                                                            PricePlanCost pricePlanCost =
                                                                    PricePlanCost.fromUsingFactor(
                                                                            CURRENCY, it, rp);

                                                            ProductId productId =
                                                                    productBySubscriptionPlan
                                                                            .computeIfAbsent(
                                                                                    it,
                                                                                    stripeInitService
                                                                                            ::createProduct);
                                                            PriceId priceId =
                                                                    stripeInitService
                                                                            .createProductPrice(
                                                                                    new ProductPriceDetails(
                                                                                            productId,
                                                                                            it,
                                                                                            rp,
                                                                                            pricePlanCost));

                                                            return SubscriptionPricePlanEntity
                                                                    .create(
                                                                            priceId,
                                                                            productId,
                                                                            pricePlanCost,
                                                                            it,
                                                                            rp);
                                                        }))
                        .toList();
        subscriptionPricePlanRepository.deleteAll();
        subscriptionPricePlanRepository.saveAll(list);
    }

    private static List<SubscriptionPlan> getPaidSubscriptionPlans() {
        return Arrays.stream(SubscriptionPlan.values()).filter(it -> it.getCost() > 0).toList();
    }
}
