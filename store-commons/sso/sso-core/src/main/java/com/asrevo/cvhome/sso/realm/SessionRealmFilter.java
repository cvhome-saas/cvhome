package com.asrevo.cvhome.sso.realm;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.filter.OncePerRequestFilter;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.sso.session.SessionMetadata;

import lombok.extern.slf4j.Slf4j;

/**
 * A session belongs to the realm it was created in, and to no other.
 *
 * <p>
 * Between stores, isolation already rests on the session cookie being host-scoped: one host is one store, and a
 * cookie set for {@code store-a.example} is never sent to {@code store-b.example}. That is a property of the
 * browser and of never setting a {@code Domain} on a shared parent — a single line of configuration away from
 * handing every store's sessions to every other store. This is the second lock, in the application, where it can
 * be tested.
 * </p>
 *
 * <p>
 * A mismatch ends the session and lets the request continue unauthenticated, so the shopper is asked to sign in
 * again rather than shown an error for something they did not do. Refusing outright would be the louder answer
 * and the wrong one: this is a guard against a mistake that should be impossible, and if it ever fires on a
 * legitimate request, signing in again is a recoverable outcome and a 403 is not.
 * </p>
 *
 * <p>
 * Ordered after Spring Session's own filter — before it, {@code getSession} does not reach the session store at
 * all — and before the security chain, so an ended session is already gone when authentication is attempted.
 * </p>
 */
@Slf4j
public class SessionRealmFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        RealmContext.current().ifPresent(realm -> check(request, realm));
        chain.doFilter(request, response);
    }

    private void check(HttpServletRequest request, RealmId realm) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        Object stamped = session.getAttribute(SessionMetadata.REALM);
        if (stamped == null) {
            // Not yet signed in, or a session that predates the stamp. Adopting the current realm is safe: the
            // stamp is written again at sign-in, from the realm the sign-in actually happened in.
            session.setAttribute(SessionMetadata.REALM, realm.getId());
            return;
        }
        if (!realm.getId().equals(stamped)) {
            log.warn("Session {} belongs to realm {} but arrived in realm {}; ending it.", session.getId(),
                    stamped, realm.getId());
            session.invalidate();
        }
    }

}
