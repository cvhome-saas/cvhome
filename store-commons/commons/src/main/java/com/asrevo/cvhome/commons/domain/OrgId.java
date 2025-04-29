package com.asrevo.cvhome.commons.domain;

public record OrgId(String id) {
    public static OrgId of(String id) {
        return new OrgId(id);
    }
}
