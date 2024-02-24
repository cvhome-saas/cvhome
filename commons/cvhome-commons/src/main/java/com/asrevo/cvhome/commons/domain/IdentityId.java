package com.asrevo.cvhome.commons.domain;

public record IdentityId(String id) implements Identifier {
    public static IdentityId of(String identity) {
        return new IdentityId(identity);
    }

    public static IdentityId ofSys() {
        return new IdentityId("system");
    }

    @Override
    public Object getId() {
        return this.id;
    }
}
