package com.asrevo.cvhome.uaa.audit;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Who the current request is.
 *
 * <p>
 * A bearer token with a {@code uid} claim is a person (uaa only writes {@code uid} for user tokens); one without is
 * an OAuth2 client authenticating as itself. A session principal is a person. No authentication is
 * {@link AuditActor#ANONYMOUS} — the actor of a failed login, for instance.
 * </p>
 */
@Component
public class AuditActorResolver {

    static final String UID = "uid";

    public AuditActor current() {
        return resolve(SecurityContextHolder.getContext().getAuthentication());
    }

    public AuditActor resolve(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return AuditActor.ANONYMOUS;
        }
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String uid = jwt.getClaimAsString(UID);
            if (uid == null) {
                return new AuditActor(AuditActorType.CLIENT, jwt.getSubject(), jwt.getSubject());
            }
            return new AuditActor(AuditActorType.USER, uid, jwt.getSubject());
        }
        return new AuditActor(AuditActorType.USER, null, authentication.getName());
    }

}
