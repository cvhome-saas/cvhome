package com.asrevo.cvhome.tenancy.manager.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.asrevo.cvhome.commons.domain.Identifier;
import com.asrevo.cvhome.tenancy.commons.dto.AuditEntityType;
import com.asrevo.cvhome.tenancy.commons.dto.AuditSource;

import lombok.Getter;

/**
 * Append-only record of a tenancy mutation.
 *
 * <p>
 * Suspending a store or an organization takes someone's business offline, so the current value alone is not
 * enough — what it was before, who changed it and when are the questions asked afterwards. A plain
 * {@code @Id Long} because this is a log keyed by a {@code bigserial} nothing else references.
 * </p>
 */
@Getter
@Table(schema = "tenancy", name = "tenancy_audit")
public class TenancyAuditEntity {

    @Id
    private Long id;

    @Column("entity_type")
    private AuditEntityType entityType;

    @Column("entity_id")
    private String entityId;

    @Column("action")
    private String action;

    @Column("from_state")
    private String fromState;

    @Column("to_state")
    private String toState;

    @Column("actor")
    private String actor;

    @Column("source")
    private AuditSource source;

    @Column("detail")
    private String detail;

    @Column("recorded_at")
    private Instant recordedAt;

    /**
     * The bare id, not the value object's {@code toString()}.
     *
     * <p>
     * {@code String.valueOf(someStoreId)} yields {@code ManagerStoreId[id=65f0…]} — forty-odd characters into a
     * {@code varchar(24)} column, so every audited change failed on the insert and took the change with it. The
     * unwrapping lives here so no caller has to remember it.
     * </p>
     */
    private static String idOf(Object entityId) {
        if (entityId instanceof Identifier identifier && identifier.getId() != null) {
            return identifier.getId().toString();
        }
        return entityId == null ? null : String.valueOf(entityId);
    }

    public static TenancyAuditEntity of(AuditEntityType type, Object entityId, String action, Object from, Object to,
                                        String actor, AuditSource source, String detail) {
        TenancyAuditEntity entity = new TenancyAuditEntity();
        entity.entityType = type;
        entity.entityId = idOf(entityId);
        entity.action = action;
        entity.fromState = from == null ? null : String.valueOf(from);
        entity.toState = to == null ? null : String.valueOf(to);
        entity.actor = actor;
        entity.source = source;
        entity.detail = detail;
        entity.recordedAt = Instant.now();
        return entity;
    }

}
