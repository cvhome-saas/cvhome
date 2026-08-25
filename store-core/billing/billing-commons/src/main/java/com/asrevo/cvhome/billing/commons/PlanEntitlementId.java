package com.asrevo.cvhome.billing.commons;

import org.bson.types.ObjectId;

import com.asrevo.cvhome.commons.domain.Identifier;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

/**
 * Identifies one entitlement row of a plan.
 *
 * <p>
 * A surrogate key rather than the natural {@code (plan, key)} pair, so the row is a plain aggregate that Spring Data
 * JDBC can update in place. Uniqueness of the natural pair is still enforced, by a constraint.
 * </p>
 */
public record PlanEntitlementId(@JsonSerialize(using = ToStringSerializer.class) ObjectId id) implements Identifier {

    public PlanEntitlementId(String id) {
        this(new ObjectId(id));
    }

    public static PlanEntitlementId newId() {
        return new PlanEntitlementId(new ObjectId());
    }

    @JsonSerialize(using = ToStringSerializer.class)
    @Override
    public ObjectId getId() {
        return this.id;
    }

}
