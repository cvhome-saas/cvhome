package com.asrevo.cvhome.billing.domain;

import java.time.Instant;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.asrevo.cvhome.billing.commons.PlanId;
import com.asrevo.cvhome.billing.commons.StripeProductId;
import com.asrevo.cvhome.commons.domain.BaseEntity;

import lombok.Getter;

/**
 * One plan in the catalog.
 *
 * <p>
 * Its prices and entitlements are separate aggregates rather than a mapped collection: {@code store_subscription}
 * points at a price by foreign key, and Spring Data JDBC rewrites a mapped child collection wholesale on every save,
 * which would break those references.
 * </p>
 */
@Getter
@Table(schema = "billing", name = "plan")
public class PlanEntity extends BaseEntity<PlanEntity, PlanId> {

    @Column("code")
    private String code;

    @Column("display_name")
    private String displayName;

    @Column("description")
    private String description;

    @Column("tier")
    private Integer tier;

    @Column("active")
    private boolean active;

    @Column("stripe_product_id")
    private StripeProductId stripeProductId;

    @Column("created_date")
    private Instant createdDate;

    @Column("updated_date")
    private Instant updatedDate;

    public static PlanEntity create(String code, String displayName, String description, Integer tier) {
        PlanEntity entity = new PlanEntity();
        Instant now = Instant.now();
        entity.setId(PlanId.newId());
        entity.code = code;
        entity.displayName = displayName;
        entity.description = description;
        entity.tier = tier;
        entity.active = true;
        entity.createdDate = now;
        entity.updatedDate = now;
        return entity;
    }

    /**
     * Applies the declared shape of the plan. Deliberately cannot change {@code code}: that is the plan's identity,
     * and a caller wanting a different one wants a different plan.
     */
    public PlanEntity describe(String newDisplayName, String newDescription, Integer newTier) {
        this.displayName = newDisplayName;
        this.description = newDescription;
        this.tier = newTier;
        this.active = true;
        this.updatedDate = Instant.now();
        return this;
    }

    /**
     * Records the Stripe product this plan was published as. Written once, after Stripe mints the id.
     */
    public PlanEntity publishedAs(StripeProductId productId) {
        this.stripeProductId = productId;
        this.updatedDate = Instant.now();
        return this;
    }

    /**
     * Withdraws the plan from sale. Never deletes it — existing subscribers still point here, and their invoices have
     * to keep rendering the plan they bought.
     */
    public PlanEntity deactivate() {
        this.active = false;
        this.updatedDate = Instant.now();
        return this;
    }

    @Override
    protected PlanId generateId() {
        return id;
    }

}
