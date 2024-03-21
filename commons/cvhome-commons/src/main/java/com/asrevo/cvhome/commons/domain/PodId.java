package com.asrevo.cvhome.commons.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.types.ObjectId;

public record PodId(@JsonSerialize(using = ToStringSerializer.class) ObjectId id) implements Identifier {
    public PodId(String id) {
        this(new ObjectId(id));
    }

    public static PodId newId() {
        return new PodId(new ObjectId());
    }

    @JsonSerialize(using = ToStringSerializer.class)
    @Override
    public ObjectId getId() {
        return this.id;
    }
}
