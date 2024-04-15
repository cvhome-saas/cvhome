package com.asrevo.cvhome.commons.domain;

import java.util.Set;

public record UserOrgStoreIdentity(IdentityId org, String store, Set<Roles> roles) {

    public boolean hasRole(Roles roles) {
        return this.roles.contains(roles);
    }
//    ROLE_SUPER_ADMIN, ROLE_ORG_ADMIN, ROLE_STORE_ADMIN, ROLE_STORE_MODERATOR, ROLE_STORE_RETAIL, ROLE_CUSTOMER

    public boolean isSuperAdmin() {
        return this.hasRole(Roles.ROLE_SUPER_ADMIN);
    }

    public boolean isOrgAdmin() {
        return this.hasRole(Roles.ROLE_ORG_ADMIN);
    }

    public boolean isStoreAdmin() {
        return this.hasRole(Roles.ROLE_STORE_ADMIN);
    }

    public boolean isStoreModerator() {
        return this.hasRole(Roles.ROLE_STORE_MODERATOR);
    }

    public boolean isStoreRetail() {
        return this.hasRole(Roles.ROLE_STORE_RETAIL);
    }

    public boolean isAnyStoreAdmin() {
        return isStoreAdmin() || isStoreModerator() || isStoreRetail();
    }

    public boolean isOrgAdminOrAnyStoreAdmin() {
        return isOrgAdmin() || isAnyStoreAdmin();
    }

    public boolean isCustomer() {
        return this.hasRole(Roles.ROLE_CUSTOMER);
    }
}
