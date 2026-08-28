package com.asrevo.cvhome.billing.commons.dto;

import java.io.Serializable;

import com.asrevo.cvhome.billing.commons.BillingInterval;
import com.asrevo.cvhome.billing.commons.Money;
import com.asrevo.cvhome.billing.commons.PlanPriceId;

/**
 * One purchasable price of a plan, as a client sees it.
 *
 * @param id       what a checkout or plan-change request names
 * @param amount   the recurring charge, in minor units
 * @param interval how often it recurs
 * @param trialDays free days granted by this price itself, independent of the org-level trial
 */
public record PlanPriceView(PlanPriceId id, Money amount, BillingInterval interval, Integer trialDays)
        implements Serializable {
}
