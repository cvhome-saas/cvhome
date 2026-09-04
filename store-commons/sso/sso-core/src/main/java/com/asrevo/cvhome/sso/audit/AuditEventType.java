package com.asrevo.cvhome.sso.audit;

/**
 * Everything uaa records, named the way the audit screen shows it. The wire value is the dotted form.
 */
public enum AuditEventType {

    USER_LOGIN("user.login", AuditCategory.AUTHENTICATION),
    USER_LOGIN_FAILED("user.login.failed", AuditCategory.AUTHENTICATION),
    USER_LOGOUT("user.logout", AuditCategory.AUTHENTICATION),
    USER_LOCKED("user.locked", AuditCategory.SECURITY),
    USER_UNLOCKED("user.unlocked", AuditCategory.SECURITY),
    USER_CREATED("user.created", AuditCategory.ADMIN),
    USER_UPDATED("user.updated", AuditCategory.ADMIN),
    USER_DELETED("user.deleted", AuditCategory.ADMIN),
    USER_ENABLED("user.enabled", AuditCategory.ADMIN),
    USER_DISABLED("user.disabled", AuditCategory.ADMIN),
    USER_ACTIVATED("user.activated", AuditCategory.ADMIN),
    USER_PASSWORD_RESET("user.password.reset", AuditCategory.SECURITY),
    USER_PASSWORD_RESET_LINK_ISSUED("user.password.reset_link.issued", AuditCategory.SECURITY),
    USER_EMAIL_CHANGED("user.email.changed", AuditCategory.ADMIN),
    USER_PASSWORD_CHANGED("user.password.changed", AuditCategory.SECURITY),
    USER_EMAIL_VERIFIED("user.email.verified", AuditCategory.ADMIN),
    USER_ROLE_ASSIGNED("user.role.assigned", AuditCategory.ADMIN),
    USER_ROLE_REMOVED("user.role.removed", AuditCategory.ADMIN),
    TOKEN_ISSUED("token.issued", AuditCategory.TOKENS),
    TOKEN_REFRESH_REJECTED("token.refresh.rejected", AuditCategory.TOKENS),
    TOKEN_REVOKED("token.revoked", AuditCategory.TOKENS),
    CLIENT_CREATED("client.created", AuditCategory.ADMIN),
    CLIENT_UPDATED("client.updated", AuditCategory.ADMIN),
    CLIENT_DELETED("client.deleted", AuditCategory.ADMIN),
    CLIENT_ENABLED("client.enabled", AuditCategory.ADMIN),
    CLIENT_DISABLED("client.disabled", AuditCategory.ADMIN),
    CLIENT_SECRET_ROTATED("client.secret.rotated", AuditCategory.SECURITY),
    CLIENT_AUTH_FAILED("client.auth.failed", AuditCategory.SECURITY),
    CLIENT_REDIRECT_URI_MISMATCH("client.redirect_uri.mismatch", AuditCategory.SECURITY),
    ROLE_CREATED("role.created", AuditCategory.ADMIN),
    ROLE_UPDATED("role.updated", AuditCategory.ADMIN),
    ROLE_DELETED("role.deleted", AuditCategory.ADMIN),
    ROLE_PERMISSIONS_UPDATED("role.permissions.updated", AuditCategory.ADMIN),
    IDP_CREATED("idp.created", AuditCategory.ADMIN),
    IDP_UPDATED("idp.updated", AuditCategory.ADMIN),
    IDP_DELETED("idp.deleted", AuditCategory.ADMIN),
    IDENTITY_LINKED("identity.linked", AuditCategory.AUTHENTICATION),
    IDENTITY_UNLINKED("identity.unlinked", AuditCategory.ADMIN),
    SETTINGS_UPDATED("settings.updated", AuditCategory.ADMIN),
    KEY_ROTATED("key.rotated", AuditCategory.SECURITY),
    KEY_RETIRED("key.retired", AuditCategory.SECURITY),
    SESSION_REVOKED("session.revoked", AuditCategory.SECURITY),
    INVITATION_CREATED("invitation.created", AuditCategory.ADMIN),
    INVITATION_REVOKED("invitation.revoked", AuditCategory.ADMIN),
    INVITATION_ACCEPTED("invitation.accepted", AuditCategory.AUTHENTICATION),
    RATE_LIMITED("request.rate_limited", AuditCategory.SECURITY);

    private final String wire;

    private final AuditCategory category;

    AuditEventType(String wire, AuditCategory category) {
        this.wire = wire;
        this.category = category;
    }

    public String wire() {
        return wire;
    }

    public AuditCategory category() {
        return category;
    }

    public static AuditEventType fromWire(String wire) {
        for (AuditEventType type : values()) {
            if (type.wire.equals(wire)) {
                return type;
            }
        }
        throw new IllegalArgumentException(String.format("Unknown audit event type %s", wire));
    }

    /** How the audit screen groups events. */
    public enum AuditCategory {
        AUTHENTICATION, ADMIN, TOKENS, SECURITY
    }

}
