package com.asrevo.cvhome.sso.security;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationFailureCredentialsExpiredEvent;
import org.springframework.security.authentication.event.AuthenticationFailureDisabledEvent;
import org.springframework.security.authentication.event.AuthenticationFailureLockedEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.sso.audit.AuditActor;
import com.asrevo.cvhome.sso.audit.AuditActorType;
import com.asrevo.cvhome.sso.audit.AuditEventType;
import com.asrevo.cvhome.sso.audit.AuditRecord;
import com.asrevo.cvhome.sso.audit.AuditRequestContext;
import com.asrevo.cvhome.sso.audit.AuditService;

import lombok.RequiredArgsConstructor;

/**
 * Turns Spring Security's authentication events into audit rows and lockout counts.
 *
 * <p>
 * Only password logins ({@code UsernamePasswordAuthenticationToken}) are handled here: an OAuth2 client failing to
 * authenticate at the token endpoint is a different event with a different actor, and the protocol hooks record it.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class AuthenticationAuditListener {

    static final String BAD_CREDENTIALS = "BAD_CREDENTIALS";

    static final String LOCKED = "LOCKED";

    static final String DISABLED = "DISABLED";

    static final String PASSWORD_EXPIRED = "PASSWORD_EXPIRED";

    static final String OTHER = "OTHER";

    private final LockoutService lockout;

    private final PrincipalNames principals;

    private final AuditService audit;

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        if (!(event.getAuthentication() instanceof UsernamePasswordAuthenticationToken)) {
            return;
        }
        // The principal name is the account id; lockout counters and audit rows are both keyed by the username.
        String username = principals.display(event.getAuthentication().getName());
        lockout.succeeded(username, AuditRequestContext.current().ip(), LockoutService.VIA_PASSWORD);
        audit.recordDetached(AuditRecord.of(AuditEventType.USER_LOGIN)
                .actor(new AuditActor(AuditActorType.USER, null, username))
                .user(null, username)
                .detail(LockoutService.VIA_PASSWORD));
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        if (!(event.getAuthentication() instanceof UsernamePasswordAuthenticationToken)) {
            return;
        }
        String username = event.getAuthentication().getName();
        String reason = reasonOf(event);
        if (event instanceof AuthenticationFailureBadCredentialsEvent) {
            lockout.failed(username);
        }
        audit.recordDetached(AuditRecord.of(AuditEventType.USER_LOGIN_FAILED)
                .actor(AuditActor.ANONYMOUS)
                .user(null, username)
                .failed(reason));
    }

    @EventListener
    public void onLogout(LogoutSuccessEvent event) {
        if (event.getAuthentication() == null) {
            return;
        }
        String username = principals.display(event.getAuthentication().getName());
        audit.recordDetached(AuditRecord.of(AuditEventType.USER_LOGOUT)
                .actor(new AuditActor(AuditActorType.USER, null, username))
                .user(null, username));
    }

    private static String reasonOf(AbstractAuthenticationFailureEvent event) {
        if (event instanceof AuthenticationFailureBadCredentialsEvent) {
            return BAD_CREDENTIALS;
        }
        if (event instanceof AuthenticationFailureLockedEvent) {
            return LOCKED;
        }
        if (event instanceof AuthenticationFailureDisabledEvent) {
            return DISABLED;
        }
        if (event instanceof AuthenticationFailureCredentialsExpiredEvent) {
            return PASSWORD_EXPIRED;
        }
        return OTHER;
    }

}
