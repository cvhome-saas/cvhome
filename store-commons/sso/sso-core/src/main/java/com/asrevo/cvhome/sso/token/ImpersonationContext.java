package com.asrevo.cvhome.sso.token;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;

/**
 * Who is acting as whom, kept on the authorization row the exchanged token belongs to.
 *
 * <p>
 * Written by {@link ImpersonationExchangeProvider} when the grant is issued and read back twice: by
 * {@code JwtCustomizerConfig}, which turns it into the {@code act} claim, and by
 * {@code ProtocolAuditListener}, which turns the token's revocation into the "ended" audit row. The row outlives
 * the token, so the second reading works after expiry too.
 * </p>
 *
 * <p>
 * Stored as individual string and list attributes rather than as this record: the authorization store serialises
 * attributes with type information and only the JDK's common collections are on its allow-list.
 * </p>
 *
 * @param notAfter the hard ceiling on the token's life — the operator's own token's expiry, or fifteen minutes,
 *                 whichever is sooner
 */
public record ImpersonationContext(UUID operatorId, String operatorUsername, UUID targetId, String targetUsername,
                                   String reason, Instant notAfter) {

    static final String PREFIX = "cvhome.impersonation.";

    static final String OPERATOR_ID = PREFIX.concat("operator_id");

    static final String OPERATOR_USERNAME = PREFIX.concat("operator_username");

    static final String TARGET_ID = PREFIX.concat("target_id");

    static final String TARGET_USERNAME = PREFIX.concat("target_username");

    static final String REASON = PREFIX.concat("reason");

    static final String NOT_AFTER = PREFIX.concat("not_after");

    public void writeTo(OAuth2Authorization.Builder builder) {
        builder.attribute(OPERATOR_ID, operatorId.toString())
                .attribute(OPERATOR_USERNAME, operatorUsername)
                .attribute(TARGET_ID, targetId.toString())
                .attribute(TARGET_USERNAME, targetUsername)
                .attribute(REASON, reason)
                .attribute(NOT_AFTER, notAfter.toString());
    }

    /** The context an authorization carries, or empty when it is not an impersonation. */
    public static Optional<ImpersonationContext> from(OAuth2Authorization authorization) {
        if (authorization == null || authorization.getAttribute(OPERATOR_ID) == null) {
            return Optional.empty();
        }
        return Optional.of(new ImpersonationContext(
                UUID.fromString(authorization.getAttribute(OPERATOR_ID)),
                authorization.getAttribute(OPERATOR_USERNAME),
                UUID.fromString(authorization.getAttribute(TARGET_ID)),
                authorization.getAttribute(TARGET_USERNAME),
                authorization.getAttribute(REASON),
                Instant.parse(authorization.getAttribute(NOT_AFTER))));
    }

}
