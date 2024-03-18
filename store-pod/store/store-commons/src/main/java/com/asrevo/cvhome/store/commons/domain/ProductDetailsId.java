package com.asrevo.cvhome.store.commons.domain;

import com.asrevo.cvhome.commons.domain.Identifier;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.types.ObjectId;

public record ProductDetailsId(@JsonSerialize(using = ToStringSerializer.class) ObjectId id) implements Identifier {
    public ProductDetailsId(String id) {
        this(new ObjectId(id));
    }

    public static ProductDetailsId newId() {
        return new ProductDetailsId(new ObjectId());
    }

    @JsonSerialize(using = ToStringSerializer.class)
    @Override
    public ObjectId getId() {
        return this.id;
    }
}
