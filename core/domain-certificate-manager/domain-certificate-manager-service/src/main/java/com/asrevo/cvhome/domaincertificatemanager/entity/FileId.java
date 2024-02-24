package com.asrevo.cvhome.domaincertificatemanager.entity;

import com.asrevo.cvhome.commons.domain.Identifier;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.bson.types.ObjectId;


public record FileId(@JsonSerialize(using = ToStringSerializer.class) ObjectId id) implements Identifier {
    public FileId(String id) {
        this(new ObjectId(id));
    }

    public static FileId newId() {
        return new FileId(new ObjectId());
    }

    @JsonSerialize(using = ToStringSerializer.class)
    @Override
    public ObjectId getId() {
        return this.id;
    }
}