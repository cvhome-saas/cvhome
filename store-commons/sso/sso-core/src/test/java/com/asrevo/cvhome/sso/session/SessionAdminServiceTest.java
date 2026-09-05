package com.asrevo.cvhome.sso.session;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.MapSession;
import org.springframework.session.Session;

import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.uaa.errors.SessionNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Listing and revoking a user's sessions.
 *
 * <p>
 * Sessions are indexed by the user's <em>id</em>, not their username, so renaming an account does not orphan its
 * sessions — and a revoke addressed at a session that does not belong to this user finds nothing rather than
 * ending somebody else's. {@code revokeAll} takes the session to keep, which is what lets "sign out everywhere
 * else" leave the caller signed in; passing null ends all of them, which is what a disable or a password reset
 * wants.
 * </p>
 *
 * <p>
 * An audit row is written only when something was actually revoked. A row saying "0 session(s)" on every logout
 * makes the audit log unreadable at exactly the moment somebody is searching it.
 * </p>
 */
class SessionAdminServiceTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String KEPT = "keep-me";
    private static final String OTHER = "other";
    private static final String PRINCIPAL = "principal";

    private final FindByIndexNameSessionRepository<Session> sessions = mock(FindByIndexNameSessionRepository.class);
    private final AuditService audit = mock(AuditService.class);
    private final SessionAdminService service = new SessionAdminService(sessions, audit);

    private static User user() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername("someone");
        return user;
    }

    private static MapSession session(String id, Instant lastAccessed) {
        MapSession session = new MapSession(id);
        session.setLastAccessedTime(lastAccessed);
        session.setMaxInactiveInterval(Duration.ofMinutes(30));
        return session;
    }

    private void sessionsAre(Map<String, Session> found) {
        when(sessions.findByPrincipalName(USER_ID.toString())).thenReturn(found);
    }

    @Test
    void sessionsAreIndexedByTheUsersIdSoRenamingAnAccountDoesNotOrphanThem() {
        sessionsAre(Map.of());

        service.list(user(), null);

        verify(sessions).findByPrincipalName(USER_ID.toString());
    }

    @Test
    void theListIsNewestFirstAndMarksTheCallersOwnSession() {
        Map<String, Session> found = new LinkedHashMap<>();
        found.put(OTHER, session(OTHER, Instant.EPOCH));
        found.put(KEPT, session(KEPT, Instant.EPOCH.plusSeconds(600)));
        sessionsAre(found);

        var summaries = service.list(user(), KEPT);

        assertThat(summaries).extracting(it -> it.id()).containsExactly(KEPT, OTHER);
        assertThat(summaries.getFirst().current()).isTrue();
        assertThat(summaries.getLast().current()).isFalse();
        assertThat(summaries.getFirst().expiresAt())
                .isEqualTo(Instant.EPOCH.plusSeconds(600).plus(Duration.ofMinutes(30)));
    }

    @Test
    void revokingASessionThatIsNotThisUsersFindsNothingRatherThanEndingSomebodyElses() {
        sessionsAre(Map.of(KEPT, session(KEPT, Instant.EPOCH)));

        assertThatThrownBy(() -> service.revoke(user(), "somebody-elses"))
                .isInstanceOf(SessionNotFoundException.class);
        verify(sessions, never()).deleteById(any());
    }

    @Test
    void revokingOneOfThisUsersSessionsEndsItAndRecordsIt() throws Exception {
        sessionsAre(Map.of(KEPT, session(KEPT, Instant.EPOCH)));

        service.revoke(user(), KEPT);

        verify(sessions).deleteById(KEPT);
        verify(audit).record(any());
    }

    @Test
    void signOutEverywhereElseLeavesTheCallersOwnSessionAlone() {
        Map<String, Session> found = new LinkedHashMap<>();
        found.put(KEPT, session(KEPT, Instant.EPOCH));
        found.put(OTHER, session(OTHER, Instant.EPOCH));
        sessionsAre(found);

        assertThat(service.revokeAll(user(), KEPT)).isOne();

        verify(sessions).deleteById(OTHER);
        verify(sessions, never()).deleteById(KEPT);
    }

    @Test
    void aNullSessionToKeepEndsAllOfThemWhichIsWhatADisableWants() {
        Map<String, Session> found = new LinkedHashMap<>();
        found.put(KEPT, session(KEPT, Instant.EPOCH));
        found.put(OTHER, session(OTHER, Instant.EPOCH));
        sessionsAre(found);

        assertThat(service.revokeAll(user(), null)).isEqualTo(2);

        verify(sessions).deleteById(KEPT);
        verify(sessions).deleteById(OTHER);
    }

    @Test
    void revokingNothingWritesNoAuditRowAtAll() {
        sessionsAre(Map.of(KEPT, session(KEPT, Instant.EPOCH)));

        assertThat(service.revokeAll(user(), KEPT)).isZero();

        // A row saying "0 session(s)" on every logout makes the log unreadable when somebody is searching it.
        verify(audit, never()).record(any());
    }

    @Test
    void theByPrincipalNameVariantIsForCallersThatHaveNoUserObject() {
        when(sessions.findByPrincipalName(PRINCIPAL)).thenReturn(Map.of(OTHER, session(OTHER, Instant.EPOCH)));

        assertThat(service.revokeOthers(PRINCIPAL, KEPT)).isOne();

        verify(sessions).deleteById(OTHER);
        verify(audit).record(any());
        assertThat(service.revokeOthers(PRINCIPAL, OTHER)).isZero();
    }

    @Test
    void aSessionWithNoRecordedCreationTimeFallsBackToTheContainersOwn() {
        MapSession bare = session(KEPT, Instant.EPOCH.plusSeconds(60));
        sessionsAre(Map.of(KEPT, bare));

        // The attribute is stamped by our own filter; a session created before it existed still has to render.
        assertThat(service.list(user(), null).getFirst().createdAt()).isEqualTo(bare.getCreationTime());
    }
}
