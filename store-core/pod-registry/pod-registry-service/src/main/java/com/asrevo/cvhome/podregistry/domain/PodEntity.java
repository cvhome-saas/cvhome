package com.asrevo.cvhome.podregistry.domain;

import java.time.Instant;
import java.util.Objects;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.commons.domain.EndpointType;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodEndpoint;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.podregistry.commons.PodHealthStatus;
import com.asrevo.cvhome.podregistry.commons.PodLifecycleState;
import com.asrevo.cvhome.podregistry.commons.PodVisibility;
import com.asrevo.cvhome.podregistry.commons.dto.PodView;

import lombok.Getter;
import lombok.Setter;

/**
 * A pod: one physical per-region deployment of the store-pod services, hosting many stores.
 *
 * <p>
 * The schema is pinned explicitly. {@code spring.application.name} is {@code pod-registry}, which
 * {@code common-config.yml} feeds into {@code spring.datasource.hikari.schema} — a hyphenated schema name that does
 * not exist. The DDL schema is {@code pod_registry} and every mapping must say so rather than lean on the default.
 * </p>
 */
@Getter
@Setter
@Table(schema = "pod_registry", name = "pod")
public class PodEntity extends BaseEntity<PodEntity, PodId> {

    @Column("name")
    private String name;

    @Column("endpoint")
    private String endpoint;

    @Column("endpoint_type")
    private EndpointType endpointType;

    @Column("org_id")
    private ManagerOrgId orgId;

    @Column("visibility")
    private PodVisibility visibility;

    @Column("lifecycle_state")
    private PodLifecycleState lifecycleState;

    @Column("region")
    private String region;

    @Column("capacity_max_stores")
    private Integer capacityMaxStores;

    @Column("capacity_stores")
    private int capacityStores;

    @Column("last_health_status")
    private PodHealthStatus lastHealthStatus;

    @Column("last_health_at")
    private Instant lastHealthAt;

    /**
     * Builds a row from the routing-level {@link Pod} the configuration and the admin API speak.
     *
     * <p>
     * Visibility is derived from ownership on the way in — a pod that names an org is private — because {@code Pod}
     * has no visibility field. An operator changing that afterwards goes through the update path.
     * </p>
     */
    public static PodEntity newEntity(Pod pod) {
        PodEntity entity = new PodEntity();
        entity.setId(Objects.nonNull(pod.id()) ? pod.id() : entity.generateId());
        entity.setName(pod.name());
        entity.setEndpoint(pod.endpoint().endpoint());
        entity.setEndpointType(pod.endpoint().type());
        entity.setOrgId(pod.orgId());
        entity.setVisibility(Objects.nonNull(pod.orgId()) ? PodVisibility.PRIVATE : PodVisibility.PUBLIC);
        entity.setLifecycleState(PodLifecycleState.ACTIVE);
        return entity;
    }

    @Override
    protected PodId generateId() {
        return PodId.newId();
    }

    /** The minimal routing contract the gateway and {@code StorePodClientFactory} consume. */
    public Pod toPod() {
        return new Pod(this.getId(), this.name, new PodEndpoint(this.endpoint, this.endpointType), this.orgId, null);
    }

    /** Everything the registry knows, for its own screens and APIs. */
    public PodView toView() {
        return new PodView(this.getId(), this.name, new PodEndpoint(this.endpoint, this.endpointType), this.orgId,
                this.visibility, this.lifecycleState, this.region, this.capacityMaxStores, this.capacityStores,
                this.lastHealthStatus, this.lastHealthAt);
    }

}
