package com.asrevo.cvhome.sso.audit;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.sso.security.PrincipalNames;

/**
 * Who the current request is.
 *
 * <p>
 * A bearer token with a {@code uid} claim is a person (uaa only writes {@code uid} for user tokens); one without is
 * an OAuth2 client authenticating as itself. A session principal is a person. No authentication is
 * {@link AuditActor#ANONYMOUS} — the actor of a failed login, for instance.
 * </p>
 *
 * <p>
 * A session's principal name is the account id, so the row would read as a UUID; the username is read back for the
 * name. One primary-key lookup, on a path that only runs when something is being audited — that is, on writes.
 * </p>
 */
@Component
public class AuditActorResolver {

    static final String UID = "uid";

    private final PrincipalNames principals;

    public AuditActorResolver(PrincipalNames principals) {
        this.principals = principals;
    }

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
            return new AuditActor(AuditActorType.USER, uid, principals.display(uid));
        }
        String principal = authentication.getName();
        return new AuditActor(AuditActorType.USER, principal, principals.display(principal));
    }

}
