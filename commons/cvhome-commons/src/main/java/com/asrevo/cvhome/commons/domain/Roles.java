package com.asrevo.cvhome.commons.domain;

public enum Roles {
    ROLE_ORG_ADMIN, ROLE_STORE_ADMIN, ROLE_STORE_MODERATOR, ROLE_CUSTOMER;

    public static Roles parse(String role) {
        try {
            return Roles.valueOf(role);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
