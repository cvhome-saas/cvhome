package com.asrevo.cvhome.sso.audit;

import java.time.Instant;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.Getter;
import lombok.Setter;

/**
 * One row of the audit log. Append-only: nothing updates it and only retention deletes it.
 */
@Entity
@Table(name = "audit_events")
@Getter
@Setter
public class AuditEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "event_type", nullable = false, length = 60)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 10)
    private AuditOutcome outcome;

    @Column(name = "reason_code", length = 60)
    private String reasonCode;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "actor_type", nullable = false, length = 10)
    private AuditActorType actorType;

    @Column(name = "actor_id", length = 190)
    private String actorId;

    @Column(name = "actor_name", length = 190)
    private String actorName;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "target_type", length = 20)
    private AuditTargetType targetType;

    @Column(name = "target_id", length = 190)
    private String targetId;

    @Column(name = "target_name", length = 190)
    private String targetName;

    @Column(name = "client_id", length = 100)
    private String clientId;

    @Column(length = 45)
    private String ip;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "before_json", columnDefinition = "jsonb")
    private Map<String, Object> before;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "after_json", columnDefinition = "jsonb")
    private Map<String, Object> after;

    @Column(length = 1000)
    private String detail;

    @Column(name = "trace_id", length = 64)
    private String traceId;

}
