package com.asrevo.cvhome.sso.session;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.sso.audit.AuditEventType;
import com.asrevo.cvhome.sso.audit.AuditRecord;
import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.audit.AuditTargetType;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.uaa.errors.SessionNotFoundException;

/**
 * The sessions Spring Session holds for an account, and the ending of them.
 *
 * <p>
 * Spring Session JDBC indexes sessions by principal name, which is what makes "sign out everywhere" a query
 * rather than a table scan. Ending a session deletes its row; the browser's cookie then names nothing.
 * </p>
 *
 * <p>
 * That index is the account id, so these methods take the account rather than a name. A username would be the
 * wrong key: it is unique only within a realm, and one Spring Session table serves every realm on the
 * deployment, so keying on it let a shopper of one store list and end the sessions of a same-named shopper of
 * another.
 * </p>
 */
@Service
public class SessionAdminService {

    private static final String COUNT = "%d session(s)";

    private final FindByIndexNameSessionRepository<? extends Session> sessions;

    private final AuditService audit;

    public SessionAdminService(FindByIndexNameSessionRepository<? extends Session> sessions, AuditService audit) {
        this.sessions = sessions;
        this.audit = audit;
    }

    public List<SessionSummary> list(User user, String currentSessionId) {
        Map<String, ? extends Session> found = sessions.findByPrincipalName(principalName(user));
        return found.values().stream()
                .map(s -> toSummary(s, currentSessionId))
                .sorted(Comparator.comparing(SessionSummary::lastAccessedAt).reversed())
                .toList();
    }

    /** Ends one session of {@code user}; another account's session is "not found", never "forbidden". */
    public void revoke(User user, String sessionId) throws SessionNotFoundException {
        Session session = sessions.findByPrincipalName(principalName(user)).get(sessionId);
        if (session == null) {
            throw SessionNotFoundException.of(sessionId);
        }
        sessions.deleteById(sessionId);
        audit.record(AuditRecord.of(AuditEventType.SESSION_REVOKED)
                .target(AuditTargetType.SESSION, sessionId, user.getUsername()));
    }

    /** Ends every session of {@code user} except {@code keepSessionId} (null keeps none). */
    public int revokeAll(User user, String keepSessionId) {
        int revoked = deleteOthers(principalName(user), keepSessionId);
        if (revoked > 0) {
            audit.record(AuditRecord.of(AuditEventType.SESSION_REVOKED)
                    .target(AuditTargetType.USER, principalName(user), user.getUsername())
                    .detail(String.format(COUNT, revoked)));
        }
        return revoked;
    }

    /**
     * The same, at sign-in time, when the account row has not been loaded — the principal name is already the
     * account id, and the login this enforces "one session per user" for is audited on its own.
     */
    public int revokeOthers(String principalName, String keepSessionId) {
        int revoked = deleteOthers(principalName, keepSessionId);
        if (revoked > 0) {
            audit.record(AuditRecord.of(AuditEventType.SESSION_REVOKED)
                    .target(AuditTargetType.USER, principalName, null).detail(String.format(COUNT, revoked)));
        }
        return revoked;
    }

    private int deleteOthers(String principalName, String keepSessionId) {
        int revoked = 0;
        for (String id : sessions.findByPrincipalName(principalName).keySet()) {
            if (!id.equals(keepSessionId)) {
                sessions.deleteById(id);
                revoked++;
            }
        }
        return revoked;
    }

    private static String principalName(User user) {
        return user.getId().toString();
    }

    private static SessionSummary toSummary(Session s, String currentSessionId) {
        Long createdMillis = s.getAttribute(SessionMetadata.CREATED_AT);
        Instant created = createdMillis == null ? s.getCreationTime() : Instant.ofEpochMilli(createdMillis);
        return new SessionSummary(s.getId(), created, s.getLastAccessedTime(),
                s.getLastAccessedTime().plus(s.getMaxInactiveInterval()), s.getAttribute(SessionMetadata.IP),
                s.getAttribute(SessionMetadata.USER_AGENT), s.getAttribute(SessionMetadata.VIA),
                s.getId().equals(currentSessionId));
    }

}
