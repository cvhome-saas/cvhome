package com.asrevo.cvhome.commons.domain;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.types.ObjectId;


public record OrdersEventsId(@JsonSerialize(using = ToStringSerializer.class) ObjectId id) implements Identifier {
    public OrdersEventsId(String id) {
        this(new ObjectId(id));
    }

    public static OrdersEventsId newId() {
        return new OrdersEventsId(new ObjectId());
    }

    @JsonSerialize(using = ToStringSerializer.class)
    @Override
    public ObjectId getId() {
        return this.id;
    }
}
