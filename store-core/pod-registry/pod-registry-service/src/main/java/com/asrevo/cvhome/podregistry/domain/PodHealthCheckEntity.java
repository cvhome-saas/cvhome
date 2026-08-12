package com.asrevo.cvhome.podregistry.domain;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.podregistry.commons.PodHealthStatus;

import lombok.Getter;

/**
 * One probe result.
 *
 * <p>
 * Kept separate from {@code pod.last_health_status} so the current state stays a single cheap read while the
 * history is still there to answer "was it already failing before the incident". Grows without bound and needs a
 * retention job before production — see the plan's open risks.
 * </p>
 */
@Getter
@Table(schema = "pod_registry", name = "pod_health_check")
public class PodHealthCheckEntity {

    @Id
    private Long id;

    @Column("pod_id")
    private PodId podId;

    @Column("status")
    private PodHealthStatus status;

    @Column("latency_ms")
    private Integer latencyMs;

    @Column("detail")
    private String detail;

    @Column("checked_at")
    private Instant checkedAt;

    public static PodHealthCheckEntity of(PodId podId, PodHealthStatus status, Integer latencyMs, String detail) {
        PodHealthCheckEntity entity = new PodHealthCheckEntity();
        entity.podId = podId;
        entity.status = status;
        entity.latencyMs = latencyMs;
        entity.detail = detail;
        entity.checkedAt = Instant.now();
        return entity;
    }

}
