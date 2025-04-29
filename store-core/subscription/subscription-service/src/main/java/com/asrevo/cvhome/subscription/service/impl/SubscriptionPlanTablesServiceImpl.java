package com.asrevo.cvhome.subscription.service.impl;

import com.asrevo.cvhome.subscription.commons.PriceId;
import com.asrevo.cvhome.subscription.commons.SubscriptionPlanOption;
import com.asrevo.cvhome.subscription.commons.SubscriptionPlanTables;
import com.asrevo.cvhome.subscription.domain.SubscriptionPricePlanEntity;
import com.asrevo.cvhome.subscription.repository.SubscriptionPricePlanRepository;
import com.asrevo.cvhome.subscription.service.SubscriptionPlanTablesService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@Slf4j
@AllArgsConstructor
public class SubscriptionPlanTablesServiceImpl implements SubscriptionPlanTablesService {
    private final SubscriptionPricePlanRepository subscriptionPricePlanRepository;
    private final SubscriptionPricePlanMapper subscriptionPricePlanMapper;

    @Override
    public SubscriptionPlanTables tables() {
        Stream<SubscriptionPricePlanEntity> paidPlans = StreamSupport.stream(subscriptionPricePlanRepository.findAll().spliterator(), false);
        return paidPlans
                .collect(
                        Collectors.collectingAndThen(
                                Collectors.groupingBy(SubscriptionPricePlanEntity::getRecurringPlan,
                                        Collectors.collectingAndThen(Collectors.toList(), subscriptionPricePlanMapper::toTable)),
                                tables -> new SubscriptionPlanTables(tables, SubscriptionPlanOption.FREE_OPTION)));
    }

    @Override
    public Optional<SubscriptionPlanOption> getSubscriptionPlanOption(PriceId priceId) {
        return subscriptionPricePlanRepository.findById(priceId)
                .map(subscriptionPricePlanMapper::toOption);
    }
}
