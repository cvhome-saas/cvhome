package com.asrevo.cvhome.billing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.asrevo.cvhome.billing.commons.BillingInterval;
import com.asrevo.cvhome.billing.commons.PlanId;
import com.asrevo.cvhome.billing.commons.PlanPriceId;
import com.asrevo.cvhome.billing.commons.StripePriceId;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;
import com.asrevo.cvhome.commons.domain.CurrencyCode;

public interface PlanPriceRepository extends CrudRepository<PlanPriceEntity, PlanPriceId> {

    List<PlanPriceEntity> findAllByPlanId(PlanId planId);

    Optional<PlanPriceEntity> findByStripePriceId(StripePriceId stripePriceId);

    List<PlanPriceEntity> findAllByPlanIdAndActiveTrue(PlanId planId);

    Optional<PlanPriceEntity> findByPlanIdAndCurrencyAndBillingIntervalAndActiveTrue(PlanId planId,
                                                                                     CurrencyCode currency,
                                                                                     BillingInterval billingInterval);

}
