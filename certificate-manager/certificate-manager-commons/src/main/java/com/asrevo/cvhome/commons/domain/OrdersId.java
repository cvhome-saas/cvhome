package com.asrevo.cvhome.commons.domain;

import org.bson.types.ObjectId;

public record OrdersId(ObjectId id) implements Identifier {
    public OrdersId() {
        this(new ObjectId());
    }

    public OrdersId(String id) {
        this(new ObjectId(id));
    }

    public static OrdersId newId() {
        return new OrdersId();
    }

    @Override
    public ObjectId getId() {
        return this.id;
    }
}
