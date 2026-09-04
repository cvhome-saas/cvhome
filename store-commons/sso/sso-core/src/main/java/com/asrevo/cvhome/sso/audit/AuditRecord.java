package com.asrevo.cvhome.sso.audit;

import java.util.Objects;
import java.util.UUID;

/**
 * What a caller knows about an event; the service fills in the actor, the request and the time.
 *
 * <p>
 * Built fluently and immutable-ish: every {@code with}-style method returns {@code this} so a record can be
 * assembled in one expression at the call site.
 * </p>
 */
public final class AuditRecord {

    private final AuditEventType type;

    private AuditOutcome outcome = AuditOutcome.SUCCESS;

    private String reasonCode;

    private AuditTargetType targetType;

    private String targetId;

    private String targetName;

    private String clientId;

    private Object before;

    private Object after;

    private String detail;

    private AuditActor actor;

    private AuditRecord(AuditEventType type) {
        this.type = Objects.requireNonNull(type);
    }

    public static AuditRecord of(AuditEventType type) {
        return new AuditRecord(type);
    }

    public AuditRecord failed(String reasonCode) {
        this.outcome = AuditOutcome.FAILURE;
        this.reasonCode = reasonCode;
        return this;
    }

    public AuditRecord reason(String reasonCode) {
        this.reasonCode = reasonCode;
        return this;
    }

    public AuditRecord target(AuditTargetType targetType, String targetId, String targetName) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.targetName = targetName;
        return this;
    }

    public AuditRecord user(UUID userId, String username) {
        return target(AuditTargetType.USER, userId == null ? null : userId.toString(), username);
    }

    public AuditRecord client(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /** Snapshots (DTOs, never entities) whose differing fields become the change diff. */
    public AuditRecord change(Object before, Object after) {
        this.before = before;
        this.after = after;
        return this;
    }

    public AuditRecord detail(String detail) {
        this.detail = detail;
        return this;
    }

    /** Overrides the actor the service would resolve from the request — for jobs and for failed logins. */
    public AuditRecord actor(AuditActor actor) {
        this.actor = actor;
        return this;
    }

    AuditEventType type() {
        return type;
    }

    AuditOutcome outcome() {
        return outcome;
    }

    String reasonCode() {
        return reasonCode;
    }

    AuditTargetType targetType() {
        return targetType;
    }

    String targetId() {
        return targetId;
    }

    String targetName() {
        return targetName;
    }

    String clientId() {
        return clientId;
    }

    Object before() {
        return before;
    }

    Object after() {
        return after;
    }

    String detail() {
        return detail;
    }

    AuditActor actor() {
        return actor;
    }

}
