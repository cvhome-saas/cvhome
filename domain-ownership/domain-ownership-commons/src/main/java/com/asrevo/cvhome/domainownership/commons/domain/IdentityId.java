package com.asrevo.cvhome.domainownership.commons.domain;

public record IdentityId(String identity) {
    public static IdentityId of(String identity) {
        return new IdentityId(identity);
    }

    public static IdentityId ofSys() {
        return new IdentityId("system");
    }

}
