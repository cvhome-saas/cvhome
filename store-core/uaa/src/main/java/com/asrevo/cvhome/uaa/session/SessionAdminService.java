package com.asrevo.cvhome.uaa.session;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.uaa.audit.AuditEventType;
import com.asrevo.cvhome.uaa.audit.AuditRecord;
import com.asrevo.cvhome.uaa.audit.AuditService;
import com.asrevo.cvhome.uaa.audit.AuditTargetType;
import com.asrevo.cvhome.uaa.errors.SessionNotFoundException;

/**
 * The sessions Spring Session holds for an account, and the ending of them.
 *
 * <p>
 * Spring Session JDBC indexes sessions by principal name, which is what makes "sign out everywhere" a query
 * rather than a table scan. Ending a session deletes its row; the browser's cookie then names nothing.
 * </p>
 */
@Service
public class SessionAdminService {

    private final FindByIndexNameSessionRepository<? extends Session> sessions;

    private final AuditService audit;

    public SessionAdminService(FindByIndexNameSessionRepository<? extends Session> sessions, AuditService audit) {
        this.sessions = sessions;
        this.audit = audit;
    }

    public List<SessionSummary> list(String username, String currentSessionId) {
        Map<String, ? extends Session> found = sessions.findByPrincipalName(username);
        return found.values().stream()
                .map(s -> toSummary(s, currentSessionId))
                .sorted(Comparator.comparing(SessionSummary::lastAccessedAt).reversed())
                .toList();
    }

    /** Ends one session of {@code username}; another account's session is "not found", never "forbidden". */
    public void revoke(String username, String sessionId) throws SessionNotFoundException {
        Session session = sessions.findByPrincipalName(username).get(sessionId);
        if (session == null) {
            throw SessionNotFoundException.of(sessionId);
        }
        sessions.deleteById(sessionId);
        audit.record(AuditRecord.of(AuditEventType.SESSION_REVOKED).target(AuditTargetType.SESSION, sessionId, username));
    }

    /** Ends every session of {@code username} except {@code keepSessionId} (null keeps none). */
    public int revokeAll(String username, String keepSessionId) {
        int revoked = 0;
        for (String id : sessions.findByPrincipalName(username).keySet()) {
            if (!id.equals(keepSessionId)) {
                sessions.deleteById(id);
                revoked++;
            }
        }
        if (revoked > 0) {
            audit.record(AuditRecord.of(AuditEventType.SESSION_REVOKED).target(AuditTargetType.USER, null, username)
                    .detail(String.format("%d session(s)", revoked)));
        }
        return revoked;
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
