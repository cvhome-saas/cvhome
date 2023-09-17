package com.asrevo.cvhome.commons.domain;

import org.bson.types.ObjectId;

public record CertificateId(ObjectId id) implements Identifier {
    public CertificateId() {
        this(new ObjectId());
    }

    public CertificateId(String id) {
        this(new ObjectId(id));
    }

    public static CertificateId newId() {
        return new CertificateId();
    }

    @Override
    public ObjectId getId() {
        return this.id;
    }
}
