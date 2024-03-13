package com.asrevo.cvhome.product.commons.domain;

import com.asrevo.cvhome.commons.domain.Identifier;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.types.ObjectId;

public record ProductFeatureId(@JsonSerialize(using = ToStringSerializer.class) ObjectId id) implements Identifier {
    public ProductFeatureId(String id) {
        this(new ObjectId(id));
    }

    public static ProductFeatureId newId() {
        return new ProductFeatureId(new ObjectId());
    }

    @JsonSerialize(using = ToStringSerializer.class)
    @Override
    public ObjectId getId() {
        return this.id;
    }
}
