package com.asrevo.cvhome.commons.domain;

public enum Roles {

    /**
     * Platform support. Holds {@code users:impersonate} and nothing a merchant screen authorises on: the role is a way
     * in to act as a merchant (read-only), never a way to act as itself on a store.
     */
    ROLE_SUPER_ADMIN, ROLE_SUPPORT, ROLE_ORG_ADMIN, ROLE_STORE_ADMIN, ROLE_STORE_MODERATOR, ROLE_STORE_RETAIL,
    ROLE_CUSTOMER, SCOPE_STORE_CORE, SCOPE_STORE_POD;

    public static Roles parse(String role) {
        try {
            return Roles.valueOf(role);
        } catch (IllegalArgumentException _) {
            return null;
        }
    }

}
