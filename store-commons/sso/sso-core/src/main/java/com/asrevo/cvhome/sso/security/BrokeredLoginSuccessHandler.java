package com.asrevo.cvhome.sso.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.sso.audit.AuditActor;
import com.asrevo.cvhome.sso.audit.AuditActorType;
import com.asrevo.cvhome.sso.audit.AuditEventType;
import com.asrevo.cvhome.sso.audit.AuditRecord;
import com.asrevo.cvhome.sso.audit.AuditRequestContext;
import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.idp.PendingLink;
import com.asrevo.cvhome.sso.session.SessionAdminService;
import com.asrevo.cvhome.sso.session.SessionMetadata;
import com.asrevo.cvhome.sso.settings.RealmSettings;
import com.asrevo.cvhome.sso.settings.SettingsService;

/**
 * After a brokered login: the same session as a password login, holding the same kind of principal.
 *
 * <p>
 * The login filter has authenticated a {@link BrokeredPrincipal}. Here it becomes a
 * {@code UsernamePasswordAuthenticationToken} over the account's {@code UserDetails} — the shape the authorization
 * server's Jackson allow-list, the token customizer and every {@code /me} caller already understand — and the context
 * is saved again, because the filter saved it before this ran. Then the session is stamped, lockout counters reset,
 * the login audited, and the saved {@code /oauth2/authorize} resumes through the shared request cache.
 * </p>
 */
@Component
public class BrokeredLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    public static final String VIA_PREFIX = "IDP:";

    private final JpaUserDetailsService userDetails;

    private final SettingsService settings;

    private final SessionAdminService sessions;

    private final LockoutService lockout;

    private final AuditService audit;

    private final HttpSessionSecurityContextRepository contexts = new HttpSessionSecurityContextRepository();

    public BrokeredLoginSuccessHandler(JpaUserDetailsService userDetails, SettingsService settings,
                                       SessionAdminService sessions, LockoutService lockout, AuditService audit,
                                       RequestCache requestCache) {
        this.userDetails = userDetails;
        this.settings = settings;
        this.sessions = sessions;
        this.lockout = lockout;
        this.audit = audit;
        setRequestCache(requestCache);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        String username = authentication.getName();
        String via = authentication.getPrincipal() instanceof BrokeredPrincipal brokered
                ? VIA_PREFIX + brokered.providerAlias() : VIA_PREFIX;
        Authentication local = establish(request, response, username, via);
        request.getSession().removeAttribute(PendingLink.SESSION_KEY);
        super.onAuthenticationSuccess(request, response, local);
    }

    /**
     * Makes the session a signed-in one for {@code username}: the local principal in the context and saved to the
     * session, the session stamped, the realm's policy applied, the login counted and audited. Shared with the
     * link-confirmation call, which signs in the same way once the password checks out.
     */
    public Authentication establish(HttpServletRequest request, HttpServletResponse response, String username, String via) {
        UserDetails details = userDetails.loadUserByUsername(username);
        Authentication local = UsernamePasswordAuthenticationToken.authenticated(details, null, details.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(local);
        SecurityContextHolder.setContext(context);
        contexts.saveContext(context, request, response);

        RealmSettings.Sessions policy = settings.current().sessions();
        HttpSession session = request.getSession();
        SessionMetadata.stamp(session, request, via);
        session.setMaxInactiveInterval(policy.idleSeconds());
        if (policy.singleSessionPerUser()) {
            sessions.revokeAll(username, session.getId());
        }
        lockout.succeeded(username, AuditRequestContext.current().ip(), via);
        audit.recordDetached(AuditRecord.of(AuditEventType.USER_LOGIN)
                .actor(new AuditActor(AuditActorType.USER, null, username))
                .user(null, username)
                .detail(via));
        return local;
    }

}
