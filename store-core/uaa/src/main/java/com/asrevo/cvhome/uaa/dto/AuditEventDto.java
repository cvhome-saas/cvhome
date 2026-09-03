package com.asrevo.cvhome.uaa.dto;

import java.time.Instant;
import java.util.Map;

import com.asrevo.cvhome.uaa.audit.AuditActorType;
import com.asrevo.cvhome.uaa.audit.AuditEventType;
import com.asrevo.cvhome.uaa.audit.AuditOutcome;
import com.asrevo.cvhome.uaa.audit.AuditTargetType;

/** One audit row as the console reads it. {@code before}/{@code after} are the diff, never the whole record. */
public record AuditEventDto(long id, Instant occurredAt, String eventType, AuditEventType.AuditCategory category,
                            AuditOutcome outcome, String reasonCode, AuditActorType actorType, String actorId,
                            String actorName, AuditTargetType targetType, String targetId, String targetName,
                            String clientId, String ip, String userAgent, Map<String, Object> before,
                            Map<String, Object> after, String detail, String traceId) {
}
