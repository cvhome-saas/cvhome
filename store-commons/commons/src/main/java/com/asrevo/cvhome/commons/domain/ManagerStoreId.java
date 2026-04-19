package com.asrevo.cvhome.commons.domain;

import org.bson.types.ObjectId;

import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

public record ManagerStoreId(@JsonSerialize(using = ToStringSerializer.class) ObjectId id) implements Identifier {
    public ManagerStoreId(String id) {
        this(new ObjectId(id));
    }

    public static ManagerStoreId newId() {
        return new ManagerStoreId(new ObjectId());
    }

    @JsonSerialize(using = ToStringSerializer.class)
    @Override
    public ObjectId getId() {
        return this.id;
    }
}
