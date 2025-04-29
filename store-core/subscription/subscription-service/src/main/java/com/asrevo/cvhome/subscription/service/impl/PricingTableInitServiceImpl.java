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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@Service
@AllArgsConstructor
@Slf4j
public class PricingTableInitServiceImpl implements PricingTableInitService {
    private final static String CURRENCY = "usd";
    private final SubscriptionPricePlanRepository subscriptionPricePlanRepository;
    private final StripeInitService stripeInitService;

    @Transactional
    @Override
    public void init() {
        if (subscriptionPricePlanRepository.count() == 0) {

            log.info("Stripe subscription plan is empty");
            createNewPricingTable();

        } else {

            log.info("Stripe subscription plan is already created will validate them");
            boolean isValid = validateCurrentPricingTable();
            if (isValid) {
                log.info("Stripe subscription plan is valid");
                return;
            }

            log.info("Stripe subscription plan is not valid will delete all and re initialize");
            subscriptionPricePlanRepository.deleteAll();
            createNewPricingTable();
        }

    }

    private boolean validateCurrentPricingTable() {
        List<PriceId> prices = StreamSupport.stream(subscriptionPricePlanRepository.findAll().spliterator(), false)
                .map(SubscriptionPricePlanEntity::getId)
                .toList();
        long expectedPrices = getPaidSubscriptionPlans().stream().flatMap(it -> Arrays.stream(RecurringPlan.values())).count();

        if (prices.size() != expectedPrices) {
            return false;
        }

        return prices.stream().allMatch(stripeInitService::exist);
    }

    private void createNewPricingTable() {
        List<SubscriptionPlan> subscriptionPlans = getPaidSubscriptionPlans();

        List<RecurringPlan> recurringPlans = Arrays.stream(RecurringPlan.values())
                .toList();

        Map<SubscriptionPlan, ProductId> productBySubscriptionPlan = new HashMap<>();


        List<SubscriptionPricePlanEntity> list = subscriptionPlans.stream()
                .flatMap(it ->
                        recurringPlans.stream()
                                .map(rp -> {

                                    PricePlanCost pricePlanCost = PricePlanCost.fromUsingFactor(CURRENCY, it, rp);

                                    ProductId productId = productBySubscriptionPlan.computeIfAbsent(it, stripeInitService::createProduct);
                                    PriceId priceId = stripeInitService.createPrice(productId, it, rp, pricePlanCost);

                                    return SubscriptionPricePlanEntity.create(priceId, productId, pricePlanCost, it, rp);

                                }))
                .toList();

        subscriptionPricePlanRepository.saveAll(list);
    }

    private static List<SubscriptionPlan> getPaidSubscriptionPlans() {
        return Arrays.stream(SubscriptionPlan.values())
                .filter(it -> it.getCost() > 0)
                .toList();
    }
}

