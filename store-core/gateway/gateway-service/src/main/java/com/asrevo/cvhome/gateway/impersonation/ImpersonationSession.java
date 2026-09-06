package com.asrevo.cvhome.gateway.impersonation;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

/**
 * What the gateway keeps, per session, while it acts as somebody else: the operator's own context and client so the
 * swap can be undone without a login, and the exchanged token so it can be revoked.
 *
 * <p>
 * A session attribute and nothing more durable. Sessions live in the JVM, so a gateway restart ends every
 * impersonation — which is the intended behaviour, not a gap.
 * </p>
 *
 * @param originalClient the operator's own authorized client, re-saved on the way back so a refresh that happened
 *                       during the impersonation is not lost
 */
public record ImpersonationSession(SecurityContext original, OAuth2AuthorizedClient originalClient,
                                   OAuth2AuthenticationToken impersonated, String token, ImpersonationView view) {

    /** The session attribute this lives under. */
    public static final String ATTRIBUTE = "cvhome.impersonation";

}
