package com.asrevo.cvhome.commons.domain;

public enum Groups {
    SUPER_ADMIN, ORG_ADMIN, STORE_ADMIN, STORE_MODERATOR, STORE_RETAIL, CUSTOMER;

    public static Groups parse(String group) {
        try {
            return Groups.valueOf(group);
        } catch (Exception e) {
            return null;
        }
    }
}
