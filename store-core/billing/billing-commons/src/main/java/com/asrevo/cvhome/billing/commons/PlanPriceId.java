package com.asrevo.cvhome.billing.commons;

import org.bson.types.ObjectId;

import com.asrevo.cvhome.commons.domain.Identifier;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

/**
 * Identifies one purchasable price of a plan — a (plan, currency, interval) triple.
 *
 * <p>
 * A subscription points at a price, never at a plan alone: the plan says <em>what</em> the customer gets, the price
 * says what they pay and how often. Changing an amount mints a new price rather than editing this one, because Stripe
 * prices are immutable and existing subscribers must keep the terms they agreed to.
 * </p>
 */
public record PlanPriceId(@JsonSerialize(using = ToStringSerializer.class) ObjectId id) implements Identifier {

    public PlanPriceId(String id) {
        this(new ObjectId(id));
    }

    public static PlanPriceId newId() {
        return new PlanPriceId(new ObjectId());
    }

    @JsonSerialize(using = ToStringSerializer.class)
    @Override
    public ObjectId getId() {
        return this.id;
    }

}
