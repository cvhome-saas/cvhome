package com.asrevo.cvhome.manager.commons.domain;

public enum Groups {
    ORG_ADMIN, STORE_ADMIN, STORE_MODERATOR, CUSTOMER;

    public static Groups parse(String group) {
        try {
            return Groups.valueOf(group);
        } catch (Exception e) {
            return null;
        }
    }
}
