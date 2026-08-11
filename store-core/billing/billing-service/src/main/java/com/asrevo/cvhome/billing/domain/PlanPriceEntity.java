package com.asrevo.cvhome.billing.domain;

import java.time.Instant;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.asrevo.cvhome.billing.commons.BillingInterval;
import com.asrevo.cvhome.billing.commons.Money;
import com.asrevo.cvhome.billing.commons.PlanId;
import com.asrevo.cvhome.billing.commons.PlanPriceId;
import com.asrevo.cvhome.billing.commons.StripePriceId;
import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.commons.domain.CurrencyCode;

import lombok.Getter;

/**
 * One purchasable price of a plan: an amount, a currency and an interval.
 *
 * <p>
 * Amounts are never edited. Stripe prices are immutable, so changing what a plan costs mints a new row here and
 * deactivates the old one — which also happens to be the behaviour customers are owed, since it leaves existing
 * subscribers on the terms they agreed to until they choose to move.
 * </p>
 */
@Getter
@Table(schema = "billing", name = "plan_price")
public class PlanPriceEntity extends BaseEntity<PlanPriceEntity, PlanPriceId> {

    @Column("plan_id")
    private PlanId planId;

    @Column("currency")
    private CurrencyCode currency;

    @Column("unit_amount")
    private Long unitAmount;

    @Column("billing_interval")
    private BillingInterval billingInterval;

    @Column("trial_days")
    private Integer trialDays;

    @Column("active")
    private boolean active;

    @Column("stripe_price_id")
    private StripePriceId stripePriceId;

    @Column("created_date")
    private Instant createdDate;

    @Column("updated_date")
    private Instant updatedDate;

    public static PlanPriceEntity create(PlanId planId, CurrencyCode currency, Long unitAmount,
                                         BillingInterval billingInterval, Integer trialDays) {
        PlanPriceEntity entity = new PlanPriceEntity();
        Instant now = Instant.now();
        entity.setId(PlanPriceId.newId());
        entity.planId = planId;
        entity.currency = currency;
        entity.unitAmount = unitAmount;
        entity.billingInterval = billingInterval;
        entity.trialDays = trialDays;
        entity.active = true;
        entity.createdDate = now;
        entity.updatedDate = now;
        return entity;
    }

    /**
     * Records the Stripe price this was published as. Written once, after Stripe mints the id.
     */
    public PlanPriceEntity publishedAs(StripePriceId priceId) {
        this.stripePriceId = priceId;
        this.updatedDate = Instant.now();
        return this;
    }

    /**
     * Withdraws the price from sale, leaving it readable for the subscribers still on it.
     */
    public PlanPriceEntity deactivate() {
        this.active = false;
        this.updatedDate = Instant.now();
        return this;
    }

    public Money amount() {
        return new Money(currency, unitAmount);
    }

    @Override
    protected PlanPriceId generateId() {
        return id;
    }

}
