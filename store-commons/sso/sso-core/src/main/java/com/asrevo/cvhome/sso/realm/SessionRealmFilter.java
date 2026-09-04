package com.asrevo.cvhome.sso.realm;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.filter.OncePerRequestFilter;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.errors.web.ProblemDetailFactory;
import com.asrevo.cvhome.sso.session.SessionMetadata;
import com.asrevo.cvhome.uaa.errors.UaaErrors;

import tools.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * A signed-in session belongs to the realm it was signed in to, and to no other.
 *
 * <p>
 * Between stores, isolation already rests on the session cookie being host-scoped: one host is one store, and a
 * cookie set for one is never sent to the other. That is a property of the browser and of never setting a
 * {@code Domain} on the shared parent — one line of configuration away from handing every store's sessions to
 * every other store. This is the second lock, in the application, where it can be tested.
 * </p>
 *
 * <p>
 * Only <em>stamped</em> sessions are checked, and the stamp is written at sign-in. An anonymous session — the one
 * {@code /oauth2/authorize} creates to hold the saved request before anybody has authenticated — carries nothing
 * worth protecting and is left alone. Checking it would break the hand-off, where the realm is resolved from the
 * form the browser is about to post.
 * </p>
 *
 * <p>
 * A mismatch refuses the request and <strong>leaves the session standing</strong>. Ending it was the first
 * instinct and the wrong one: a session anybody can destroy by naming another store in a query parameter is a
 * forced-logout button. Refusing costs the caller the request and costs the session's owner nothing.
 * </p>
 */
@Slf4j
public class SessionRealmFilter extends OncePerRequestFilter {

    private static final String MESSAGE = "This session belongs to another store.";

    private final ProblemDetailFactory problems;

    private final ObjectMapper json;

    public SessionRealmFilter(ProblemDetailFactory problems, ObjectMapper json) {
        this.problems = problems;
        this.json = json;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        RealmId realm = RealmContext.current().orElse(null);
        if (realm == null || belongsHere(request, realm)) {
            chain.doFilter(request, response);
            return;
        }
        refuse(response);
    }

    private static boolean belongsHere(HttpServletRequest request, RealmId realm) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return true;
        }
        Object stamped = session.getAttribute(SessionMetadata.REALM);
        if (stamped == null) {
            return true;
        }
        if (realm.getId().equals(stamped)) {
            return true;
        }
        log.warn("Session {} was signed in to realm {} and arrived in realm {}; refusing.", session.getId(),
                stamped, realm.getId());
        return false;
    }

    private void refuse(HttpServletResponse response) throws IOException {
        ProblemDetail problem = problems.create(UaaErrors.CROSS_STORE_REQUEST, MESSAGE, Map.of(), List.of(),
                problems.traceId());
        response.setStatus(UaaErrors.CROSS_STORE_REQUEST.category().httpStatus());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        json.writeValue(response.getOutputStream(), problem);
    }

}
