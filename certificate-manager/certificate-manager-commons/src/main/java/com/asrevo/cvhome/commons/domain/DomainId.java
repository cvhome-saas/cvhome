package com.asrevo.cvhome.commons.domain;

import org.bson.types.ObjectId;

public record DomainId(ObjectId id) implements Identifier {
    public DomainId() {
        this(new ObjectId());
    }

    public DomainId(String id) {
        this(new ObjectId(id));
    }

    public static DomainId newId() {
        return new DomainId();
    }

    @Override
    public ObjectId getId() {
        return this.id;
    }
}
