package com.asrevo.cvhome.certificatemanager.commons.domain;

import com.asrevo.cvhome.commons.domain.Identifier;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.types.ObjectId;


public record OrdersId(@JsonSerialize(using = ToStringSerializer.class) ObjectId id) implements Identifier {
    public OrdersId(String id) {
        this(new ObjectId(id));
    }

    public static OrdersId newId() {
        return new OrdersId(new ObjectId());
    }

    @JsonSerialize(using = ToStringSerializer.class)
    @Override
    public ObjectId getId() {
        return this.id;
    }
}
