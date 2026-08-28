package com.asrevo.cvhome.billing.commons;

import org.bson.types.ObjectId;

import com.asrevo.cvhome.commons.domain.Identifier;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

/**
 * Identifies a plan in the catalog. Opaque to callers: the human-facing handle is {@code plan.code}, which is what a
 * pricing page or a support conversation uses, while this id is what rows point at.
 */
public record PlanId(@JsonSerialize(using = ToStringSerializer.class) ObjectId id) implements Identifier {

    public PlanId(String id) {
        this(new ObjectId(id));
    }

    public static PlanId newId() {
        return new PlanId(new ObjectId());
    }

    @JsonSerialize(using = ToStringSerializer.class)
    @Override
    public ObjectId getId() {
        return this.id;
    }

}
