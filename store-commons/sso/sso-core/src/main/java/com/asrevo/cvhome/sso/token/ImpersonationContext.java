package com.asrevo.cvhome.sso.token;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;

/**
 * Who is acting as whom, kept on the authorization row the exchanged token belongs to.
 *
 * <p>
 * Written by {@link ImpersonationExchangeProvider} when the grant is issued and read back twice: by
 * {@code JwtCustomizerConfig}, which turns it into the {@code act} and {@code act_mode} claims and the read-mode role
 * override, and by {@code ProtocolAuditListener}, which turns the token's revocation into the "ended" audit row. The
 * row outlives the token, so the second reading works after expiry too.
 * </p>
 *
 * <p>
 * Stored as individual string and list attributes rather than as this record: the authorization store serialises
 * attributes with type information and only the JDK's common collections are on its allow-list.
 * </p>
 *
 * @param roles       the roles the token carries, or empty for the target's own (write mode)
 * @param permissions the permissions the token carries, or empty for the target's own (write mode)
 * @param notAfter    the hard ceiling on the token's life — the operator's own token's expiry, or fifteen minutes,
 *                    whichever is sooner
 */
public record ImpersonationContext(UUID operatorId, String operatorUsername, UUID targetId, String targetUsername,
                                   String store, ImpersonationMode mode, String reason, Instant notAfter,
                                   List<String> roles, List<String> permissions) {

    static final String PREFIX = "cvhome.impersonation.";

    static final String OPERATOR_ID = PREFIX.concat("operator_id");

    static final String OPERATOR_USERNAME = PREFIX.concat("operator_username");

    static final String TARGET_ID = PREFIX.concat("target_id");

    static final String TARGET_USERNAME = PREFIX.concat("target_username");

    static final String STORE = PREFIX.concat("store");

    static final String MODE = PREFIX.concat("mode");

    static final String REASON = PREFIX.concat("reason");

    static final String NOT_AFTER = PREFIX.concat("not_after");

    static final String ROLES = PREFIX.concat("roles");

    static final String PERMISSIONS = PREFIX.concat("permissions");

    public ImpersonationContext {
        roles = roles == null ? List.of() : List.copyOf(roles);
        permissions = permissions == null ? List.of() : List.copyOf(permissions);
    }

    /** Whether the token's roles and permissions are overridden rather than the target's own. */
    public boolean overridesRoles() {
        return !roles.isEmpty();
    }

    public void writeTo(OAuth2Authorization.Builder builder) {
        builder.attribute(OPERATOR_ID, operatorId.toString())
                .attribute(OPERATOR_USERNAME, operatorUsername)
                .attribute(TARGET_ID, targetId.toString())
                .attribute(TARGET_USERNAME, targetUsername)
                .attribute(STORE, store)
                .attribute(MODE, mode.wire())
                .attribute(REASON, reason)
                .attribute(NOT_AFTER, notAfter.toString())
                .attribute(ROLES, new ArrayList<>(roles))
                .attribute(PERMISSIONS, new ArrayList<>(permissions));
    }

    /** The context an authorization carries, or empty when it is not an impersonation. */
    public static Optional<ImpersonationContext> from(OAuth2Authorization authorization) {
        if (authorization == null || authorization.getAttribute(OPERATOR_ID) == null) {
            return Optional.empty();
        }
        return ImpersonationMode.fromWire(authorization.getAttribute(MODE)).map(mode -> new ImpersonationContext(
                UUID.fromString(authorization.getAttribute(OPERATOR_ID)),
                authorization.getAttribute(OPERATOR_USERNAME),
                UUID.fromString(authorization.getAttribute(TARGET_ID)),
                authorization.getAttribute(TARGET_USERNAME),
                authorization.getAttribute(STORE),
                mode,
                authorization.getAttribute(REASON),
                Instant.parse(authorization.getAttribute(NOT_AFTER)),
                authorization.getAttribute(ROLES),
                authorization.getAttribute(PERMISSIONS)));
    }

}
