package com.asrevo.cvhome.podregistry.domain;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.podregistry.commons.AuditSource;
import com.asrevo.cvhome.podregistry.commons.PodLifecycleState;

import lombok.Getter;

/**
 * Append-only record of a pod's lifecycle changing.
 *
 * <p>
 * Draining or decommissioning a pod changes where every future store lands, so who did it and when is worth
 * keeping. A plain {@code @Id Long} rather than the usual value-object id: this is a log, its key is a
 * {@code bigserial} the database assigns, and nothing outside the table ever refers to a row by it.
 * </p>
 */
@Getter
@Table(schema = "pod_registry", name = "pod_audit")
public class PodAuditEntity {

    @Id
    private Long id;

    @Column("pod_id")
    private PodId podId;

    @Column("from_lifecycle")
    private PodLifecycleState fromLifecycle;

    @Column("to_lifecycle")
    private PodLifecycleState toLifecycle;

    @Column("source")
    private AuditSource source;

    @Column("actor")
    private String actor;

    @Column("detail")
    private String detail;

    @Column("recorded_at")
    private Instant recordedAt;

    public static PodAuditEntity of(PodId podId, PodLifecycleState from, PodLifecycleState to, AuditSource source,
                                    String actor, String detail) {
        PodAuditEntity entity = new PodAuditEntity();
        entity.podId = podId;
        entity.fromLifecycle = from;
        entity.toLifecycle = to;
        entity.source = source;
        entity.actor = actor;
        entity.detail = detail;
        entity.recordedAt = Instant.now();
        return entity;
    }

}
