package com.asrevo.cvhome.domainownership.commons.domain;

import com.asrevo.cvhome.commons.domain.Identifier;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.types.ObjectId;

public record OwnerId(@JsonSerialize(using = ToStringSerializer.class) ObjectId id) implements Identifier {
    public OwnerId(String id) {
        this(new ObjectId(id));
    }

    public static OwnerId newId() {
        return new OwnerId(new ObjectId());
    }

    @JsonSerialize(using = ToStringSerializer.class)
    @Override
    public ObjectId getId() {
        return this.id;
    }
}
