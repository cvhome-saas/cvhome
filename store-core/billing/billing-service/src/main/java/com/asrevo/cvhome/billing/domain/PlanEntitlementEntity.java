package com.asrevo.cvhome.billing.domain;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.asrevo.cvhome.billing.commons.EntitlementKey;
import com.asrevo.cvhome.billing.commons.EntitlementValue;
import com.asrevo.cvhome.billing.commons.PlanEntitlementId;
import com.asrevo.cvhome.billing.commons.PlanId;
import com.asrevo.cvhome.commons.domain.BaseEntity;

import lombok.Getter;

/**
 * What one plan grants for one {@link EntitlementKey}.
 *
 * <p>
 * At most one of the two value columns is set; a {@code CHECK} constraint enforces the shape. Both being null means
 * unlimited, which is why an absent row and a zero limit are not the same statement.
 * </p>
 */
@Getter
@Table(schema = "billing", name = "plan_entitlement")
public class PlanEntitlementEntity extends BaseEntity<PlanEntitlementEntity, PlanEntitlementId> {

    @Column("plan_id")
    private PlanId planId;

    @Column("entitlement_key")
    private EntitlementKey entitlementKey;

    @Column("limit_value")
    private Integer limitValue;

    @Column("flag_value")
    private Boolean flagValue;

    public static PlanEntitlementEntity create(PlanId planId, EntitlementValue value) {
        PlanEntitlementEntity entity = new PlanEntitlementEntity();
        entity.setId(PlanEntitlementId.newId());
        entity.planId = planId;
        entity.entitlementKey = value.key();
        entity.limitValue = value.limitValue();
        entity.flagValue = value.flagValue();
        return entity;
    }

    public PlanEntitlementEntity grant(EntitlementValue value) {
        this.limitValue = value.limitValue();
        this.flagValue = value.flagValue();
        return this;
    }

    public EntitlementValue value() {
        return new EntitlementValue(entitlementKey, limitValue, flagValue);
    }

    @Override
    protected PlanEntitlementId generateId() {
        return id;
    }

}
