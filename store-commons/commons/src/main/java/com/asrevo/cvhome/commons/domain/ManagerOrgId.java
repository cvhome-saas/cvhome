package com.asrevo.cvhome.commons.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.types.ObjectId;

public record ManagerOrgId(@JsonSerialize(using = ToStringSerializer.class) ObjectId id)
        implements Identifier {
    public ManagerOrgId(String id) {
        this(new ObjectId(id));
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
