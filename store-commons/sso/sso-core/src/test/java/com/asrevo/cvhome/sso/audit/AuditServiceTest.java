package com.asrevo.cvhome.sso.audit;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.asrevo.cvhome.errors.web.ErrorHandlingProperties;
import com.asrevo.cvhome.errors.web.ProblemDetailFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * What ends up in an audit row, and where each field comes from.
 *
 * <p>
 * Three of them are ambient rather than passed in: the actor from the security context, the IP and user agent from
 * the request, and the trace id from the same factory that stamps error responses. That last one is what lets an
 * operator take a traceId out of a failed response and find the audit row for the same request — the reason it is
 * threaded through here rather than generated locally.
 * </p>
 *
 * <p>
 * A record that names its own actor keeps it. That matters for anything raised off a request — a scheduled job, an
 * outbox handler — where the security context holds whoever happened to be signed in last, or nobody.
 * </p>
 */
class AuditServiceTest {

    private static final String POLICY = "policy";

    private static final String BAD_CREDENTIALS = "bad_credentials";

    private static final String USER_ID_CLAIM = "u-1";

    private static final String SOMEONE = "someone";

    private static final String NIGHTLY = "nightly";

    private static final String CLIENT_IP = "203.0.113.7";

    private static final String AGENT_HEADER = "User-Agent";

    private static final String A_CLIENT = "a-client";

    private static final String BY_HAND = "by hand";

    private static final Instant NOW = Instant.parse("2026-09-04T10:00:00Z");
    private static final String AGENT = "Mozilla/5.0";

    private final AuditEventRepository repository = mock(AuditEventRepository.class);
    private final AuditActorResolver actors = mock(AuditActorResolver.class);
    private final AuditDiff diff = mock(AuditDiff.class);
    private final ProblemDetailFactory problems =
            new ProblemDetailFactory(new ErrorHandlingProperties("https://errors.example.com", false));

    private final AuditService service =
            new AuditService(repository, actors, diff, problems, Clock.fixed(NOW, ZoneOffset.UTC));

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    private AuditEventEntity recorded(AuditRecord record) {
        when(diff.of(Mockito.any(), Mockito.any())).thenReturn(new AuditDiff.Diff(null, null));
        service.record(record);
        ArgumentCaptor<AuditEventEntity> captor = ArgumentCaptor.forClass(AuditEventEntity.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void theEventTypeIsStoredAsItsWireNameNotItsEnumName() {
        when(actors.current()).thenReturn(AuditActor.ANONYMOUS);

        AuditEventEntity row = recorded(AuditRecord.of(AuditEventType.USER_LOGIN));

        // The wire name is what a console filters on; renaming the constant must not orphan every stored row.
        assertThat(row.getEventType()).isEqualTo(AuditEventType.USER_LOGIN.wire());
        assertThat(row.getOccurredAt()).isEqualTo(NOW);
    }

    @Test
    void aRecordWithNoActorTakesWhoeverIsInTheSecurityContext() {
        when(actors.current()).thenReturn(new AuditActor(AuditActorType.USER, USER_ID_CLAIM, SOMEONE));

        AuditEventEntity row = recorded(AuditRecord.of(AuditEventType.USER_LOGIN));

        assertThat(row.getActorId()).isEqualTo(USER_ID_CLAIM);
        assertThat(row.getActorName()).isEqualTo(SOMEONE);
    }

    @Test
    void aRecordThatNamesItsOwnActorKeepsIt() {
        AuditEventEntity row = recorded(AuditRecord.of(AuditEventType.USER_LOGIN)
                .actor(new AuditActor(AuditActorType.SYSTEM, "job", NIGHTLY)));

        // A scheduled job must not be recorded as whoever happened to be signed in last.
        assertThat(row.getActorName()).isEqualTo(NIGHTLY);
        Mockito.verify(actors, Mockito.never()).current();
    }

    @Test
    void theRequestsAddressAndAgentAreStampedWhenThereIsARequest() {
        when(actors.current()).thenReturn(AuditActor.ANONYMOUS);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(CLIENT_IP);
        request.addHeader(AGENT_HEADER, AGENT);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        AuditEventEntity row = recorded(AuditRecord.of(AuditEventType.USER_LOGIN));

        assertThat(row.getIp()).isEqualTo(CLIENT_IP);
        assertThat(row.getUserAgent()).isEqualTo(AGENT);
    }

    @Test
    void anEventRaisedOffARequestSimplyHasNoAddress() {
        when(actors.current()).thenReturn(AuditActor.ANONYMOUS);

        AuditEventEntity row = recorded(AuditRecord.of(AuditEventType.USER_LOGIN));

        assertThat(row.getIp()).isNull();
        assertThat(row.getUserAgent()).isNull();
    }

    @Test
    void anAbsurdlyLongUserAgentIsTruncatedRatherThanFailingTheInsert() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AGENT_HEADER, "x".repeat(2000));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // The column is bounded; a client that sends a kilobyte of agent string must not fail the action itself.
        assertThat(AuditRequestContext.current().userAgent()).hasSize(512);
    }

    @Test
    void everyRowCarriesATraceIdThatMatchesTheOneAnErrorResponseWouldShow() {
        when(actors.current()).thenReturn(AuditActor.ANONYMOUS);

        AuditEventEntity row = recorded(AuditRecord.of(AuditEventType.USER_LOGIN));

        // This is what lets an operator take a traceId out of a failed response and find the row for that request.
        assertThat(row.getTraceId()).isNotBlank();
    }

    @Test
    void theRecordBuilderCarriesEveryFieldTheRowHas() {
        when(actors.current()).thenReturn(AuditActor.ANONYMOUS);
        UUID userId = UUID.randomUUID();

        AuditEventEntity row = recorded(AuditRecord.of(AuditEventType.USER_DISABLED)
                .user(userId, SOMEONE)
                .client(A_CLIENT)
                .target(AuditTargetType.USER, userId.toString(), SOMEONE)
                .detail(BY_HAND)
                .reason(POLICY));

        assertThat(row.getTargetType()).isEqualTo(AuditTargetType.USER);
        assertThat(row.getTargetId()).isEqualTo(userId.toString());
        assertThat(row.getClientId()).isEqualTo(A_CLIENT);
        assertThat(row.getDetail()).isEqualTo(BY_HAND);
        assertThat(row.getReasonCode()).isEqualTo(POLICY);
    }

    @Test
    void aFailedRecordCarriesItsOutcomeAndReason() {
        when(actors.current()).thenReturn(AuditActor.ANONYMOUS);

        AuditEventEntity row = recorded(AuditRecord.of(AuditEventType.USER_LOGIN_FAILED).failed(BAD_CREDENTIALS));

        assertThat(row.getOutcome()).isEqualTo(AuditOutcome.FAILURE);
        assertThat(row.getReasonCode()).isEqualTo(BAD_CREDENTIALS);
    }

    @Test
    void theDetachedWriteIsTheSameRowInItsOwnTransaction() {
        when(actors.current()).thenReturn(AuditActor.ANONYMOUS);
        when(diff.of(Mockito.any(), Mockito.any())).thenReturn(new AuditDiff.Diff(null, null));

        // REQUIRES_NEW, so a failure being audited still leaves a row when the caller's transaction rolls back.
        service.recordDetached(AuditRecord.of(AuditEventType.USER_LOGIN_FAILED));

        verify(repository).save(Mockito.any(AuditEventEntity.class));
    }
}
