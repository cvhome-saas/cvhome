package com.asrevo.cvhome.podregistry.domain;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import lombok.Getter;

/**
 * Which store sits on which pod — the registry's own copy, and the thing that makes capacity counting idempotent.
 *
 * <p>
 * The store id is the primary key, which is the entire point. {@code capacity_stores = capacity_stores + 1} is not
 * idempotent, and the handler that increments it runs from tenancy's outbox, which retries. Inserting here first
 * means a redelivery hits the primary key and the count is left alone.
 * </p>
 *
 * <p>
 * It duplicates a fact tenancy owns ({@code manager_store.pod_id}), which is deliberate: the alternative is
 * pod-registry reading tenancy's schema, and the copy is what lets the count be maintained without either service
 * calling the other in a request path.
 * </p>
 */
@Getter
@Table(schema = "pod_registry", name = "pod_store_placement")
public class PodStorePlacementEntity {

    @Id
    @Column("store_id")
    private StoreMerchantId storeId;

    @Column("pod_id")
    private PodId podId;

    @Column("placed_at")
    private Instant placedAt;

    public static PodStorePlacementEntity of(StoreMerchantId storeId, PodId podId) {
        PodStorePlacementEntity entity = new PodStorePlacementEntity();
        entity.storeId = storeId;
        entity.podId = podId;
        entity.placedAt = Instant.now();
        return entity;
    }

}
