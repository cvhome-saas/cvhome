package com.asrevo.cvhome.commons.domain;

import java.util.Arrays;
import java.util.Optional;

/**
 * The platform's permission catalogue, as uaa issues it in the {@code permissions} claim.
 *
 * <p>
 * A role is a named set of these; a user's effective set is the union over their roles and what those roles
 * inherit. Services still authorise on <em>roles</em> today — this vocabulary is issued so they can start reading
 * it without a token-format change, and so the consoles can hide what a principal cannot do.
 * </p>
 *
 * <p>
 * Keys are {@code area:action} and appear verbatim in the token; the enum name is the Java spelling. Adding a value
 * here is what makes it grantable — uaa validates a grant against this list, and the seed gives every value to
 * {@code SUPER_ADMIN}.
 * </p>
 */
public enum Permission {

    USERS_READ("users:read", PermissionGroup.IDENTITY, "See accounts"),
    USERS_WRITE("users:write", PermissionGroup.IDENTITY, "Create, edit, enable and disable accounts"),
    USERS_INVITE("users:invite", PermissionGroup.IDENTITY, "Invite people and issue reset links"),
    USERS_SESSIONS("users:sessions", PermissionGroup.IDENTITY, "See and end other people's sessions"),
    USERS_UNLOCK("users:unlock", PermissionGroup.IDENTITY, "Unlock a locked account"),
    USERS_IMPERSONATE("users:impersonate", PermissionGroup.IDENTITY, "Act as a merchant account inside the console"),
    ROLES_READ("roles:read", PermissionGroup.IDENTITY, "See roles and what they grant"),
    ROLES_WRITE("roles:write", PermissionGroup.IDENTITY, "Create and change roles"),
    CLIENTS_READ("clients:read", PermissionGroup.CLIENTS, "See registered applications"),
    CLIENTS_WRITE("clients:write", PermissionGroup.CLIENTS, "Register and change applications"),
    CLIENTS_SECRETS("clients:secrets", PermissionGroup.CLIENTS, "Rotate application secrets"),
    IDPS_READ("idps:read", PermissionGroup.IDENTITY_PROVIDERS, "See identity providers"),
    IDPS_WRITE("idps:write", PermissionGroup.IDENTITY_PROVIDERS, "Add and change identity providers"),
    SETTINGS_READ("settings:read", PermissionGroup.SYSTEM, "See realm settings"),
    SETTINGS_WRITE("settings:write", PermissionGroup.SYSTEM, "Change realm settings"),
    AUDIT_READ("audit:read", PermissionGroup.SYSTEM, "Read the audit log"),
    KEYS_READ("keys:read", PermissionGroup.SYSTEM, "See signing keys"),
    KEYS_ROTATE("keys:rotate", PermissionGroup.SYSTEM, "Rotate signing keys"),
    DASHBOARD_READ("dashboard:read", PermissionGroup.SYSTEM, "See the dashboard");

    private final String key;

    private final PermissionGroup group;

    private final String description;

    Permission(String key, PermissionGroup group, String description) {
        this.key = key;
        this.group = group;
        this.description = description;
    }

    /** The wire spelling, {@code area:action}. */
    public String key() {
        return key;
    }

    public PermissionGroup group() {
        return group;
    }

    public String description() {
        return description;
    }

    public static Optional<Permission> fromKey(String key) {
        return Arrays.stream(values()).filter(p -> p.key.equals(key)).findFirst();
    }

    /** How the catalogue is grouped on screen. */
    public enum PermissionGroup {
        IDENTITY, CLIENTS, IDENTITY_PROVIDERS, SYSTEM
    }

}
