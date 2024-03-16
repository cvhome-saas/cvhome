package com.asrevo.cvhome.storepod.commons.domain;

import com.asrevo.cvhome.commons.domain.Identifier;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.types.ObjectId;

public record CategoryId(@JsonSerialize(using = ToStringSerializer.class) ObjectId id) implements Identifier {
    public CategoryId(String id) {
        this(new ObjectId(id));
    }

    public static CategoryId newId() {
        return new CategoryId(new ObjectId());
    }

    @JsonSerialize(using = ToStringSerializer.class)
    @Override
    public ObjectId getId() {
        return this.id;
    }
}
