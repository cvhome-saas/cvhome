package com.asrevo.cvhome.commons.domain;

import org.bson.types.ObjectId;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

public record ManagerOrgId(@JsonSerialize(using = ToStringSerializer.class) ObjectId id) implements Identifier {
    public ManagerOrgId(String id) {
        this(id == null || id.trim().length() != 24 ? null : new ObjectId(id));
    }

    public static ManagerOrgId newId() {
        return new ManagerOrgId(new ObjectId());
    }

    @JsonSerialize(using = ToStringSerializer.class)
    @Override
    public ObjectId getId() {
        return this.id;
    }
}
