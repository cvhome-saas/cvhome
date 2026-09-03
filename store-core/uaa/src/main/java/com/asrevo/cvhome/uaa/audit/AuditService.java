package com.asrevo.cvhome.uaa.audit;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.errors.web.ProblemDetailFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * Writes the audit log.
 *
 * <p>
 * Two entry points, one difference: {@link #record} joins the caller's transaction so the row and the change it
 * describes commit together — tenancy's rule, and the right one for every administrative write. {@link #recordDetached}
 * commits on its own, which is what a listener wants: a failed login has no transaction, and an event about a change
 * that then rolled back is still true about the attempt.
 * </p>
 */
@Service
@Slf4j
public class AuditService {

    private final AuditEventRepository repository;

    private final AuditActorResolver actors;

    private final AuditDiff diff;

    private final ProblemDetailFactory problems;

    private final Clock clock;

    public AuditService(AuditEventRepository repository, AuditActorResolver actors, AuditDiff diff,
                        ProblemDetailFactory problems, Clock clock) {
        this.repository = repository;
        this.actors = actors;
        this.diff = diff;
        this.problems = problems;
        this.clock = clock;
    }

    @Transactional
    public void record(AuditRecord record) {
        repository.save(toEntity(record));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordDetached(AuditRecord record) {
        repository.save(toEntity(record));
    }

    private AuditEventEntity toEntity(AuditRecord record) {
        AuditActor actor = record.actor() != null ? record.actor() : actors.current();
        AuditRequestContext request = AuditRequestContext.current();
        AuditDiff.Diff change = diff.of(record.before(), record.after());
        AuditEventEntity entity = new AuditEventEntity();
        entity.setOccurredAt(clock.instant());
        entity.setEventType(record.type().wire());
        entity.setOutcome(record.outcome());
        entity.setReasonCode(record.reasonCode());
        entity.setActorType(actor.type());
        entity.setActorId(actor.id());
        entity.setActorName(actor.name());
        entity.setTargetType(record.targetType());
        entity.setTargetId(record.targetId());
        entity.setTargetName(record.targetName());
        entity.setClientId(record.clientId());
        entity.setIp(request.ip());
        entity.setUserAgent(request.userAgent());
        entity.setBefore(change.before());
        entity.setAfter(change.after());
        entity.setDetail(record.detail());
        entity.setTraceId(problems.traceId());
        return entity;
    }

}
