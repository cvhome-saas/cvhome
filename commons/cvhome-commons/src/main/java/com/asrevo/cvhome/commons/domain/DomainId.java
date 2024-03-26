package com.asrevo.cvhome.commons.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.types.ObjectId;

public record DomainId(@JsonSerialize(using = ToStringSerializer.class) ObjectId id) implements Identifier {
    public DomainId(String id) {
        this(new ObjectId(id));
    }

    public static DomainId newId() {
        return new DomainId(new ObjectId());
    }

    @JsonSerialize(using = ToStringSerializer.class)
    @Override
    public ObjectId getId() {
        return this.id;
    }
}
