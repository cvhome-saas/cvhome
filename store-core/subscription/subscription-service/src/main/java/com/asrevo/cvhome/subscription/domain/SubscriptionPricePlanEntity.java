package com.asrevo.cvhome.subscription.domain;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.commons.domain.SubscriptionPlan;
import com.asrevo.cvhome.subscription.commons.PriceId;
import com.asrevo.cvhome.subscription.commons.PricePlanCost;
import com.asrevo.cvhome.subscription.commons.ProductId;
import com.asrevo.cvhome.subscription.commons.RecurringPlan;
import lombok.Getter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;


@Getter
@Table("subscription_price_plan")
public class SubscriptionPricePlanEntity extends BaseEntity<SubscriptionPricePlanEntity, PriceId> {
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    private ProductId productId;
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    private PricePlanCost cost;
    @Column("subscription_plan")
    private SubscriptionPlan subscriptionPlan;
    @Column("recurring_plan")
    private RecurringPlan recurringPlan;

    public static SubscriptionPricePlanEntity create(PriceId id, ProductId productId, PricePlanCost cost, SubscriptionPlan subscriptionPlan, RecurringPlan recurringPlan) {
        SubscriptionPricePlanEntity entity = new SubscriptionPricePlanEntity();
        entity.id = id;
        entity.productId = productId;
        entity.cost = cost;
        entity.subscriptionPlan = subscriptionPlan;
        entity.recurringPlan = recurringPlan;
        return entity;
    }

    @Override
    protected PriceId generateId() {
        return id;
    }
}
