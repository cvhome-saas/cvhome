package com.asrevo.cvhome.manager.commons.domain;

import com.asrevo.cvhome.commons.domain.Identifier;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.types.ObjectId;

public record ReferenceAlisId(@JsonSerialize(using = ToStringSerializer.class) ObjectId id)
        implements Identifier {
    public ReferenceAlisId(String id) {
        this(new ObjectId(id));
    }

    public static ReferenceAlisId newId() {
        return new ReferenceAlisId(new ObjectId());
    }

    @JsonSerialize(using = ToStringSerializer.class)
    @Override
    public ObjectId getId() {
        return this.id;
    }
}
