package com.asrevo.cvhome.router.commons.domain;

import com.asrevo.cvhome.commons.domain.Identifier;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.types.ObjectId;

public record ReferenceId(@JsonSerialize(using = ToStringSerializer.class) ObjectId id) implements Identifier {
    public ReferenceId(String id) {
        this(new ObjectId(id));
    }

    public static ReferenceId newId() {
        return new ReferenceId(new ObjectId());
    }

    @JsonSerialize(using = ToStringSerializer.class)
    @Override
    public ObjectId getId() {
        return this.id;
    }
}
