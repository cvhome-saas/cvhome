package com.asrevo.cvhome.commons.domain;

public record IdentityId(String id) implements Identifier {
    public static IdentityId of(String identity) {
        return new IdentityId(identity);
    }

    @Override
    public Object getId() {
        return this.id;
    }
}
