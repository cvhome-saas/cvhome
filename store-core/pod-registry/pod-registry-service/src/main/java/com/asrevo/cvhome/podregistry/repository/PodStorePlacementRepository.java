package com.asrevo.cvhome.podregistry.repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.podregistry.domain.PodStorePlacementEntity;

@Repository
public interface PodStorePlacementRepository extends CrudRepository<PodStorePlacementEntity, StoreMerchantId> {

    /**
     * Claims a store for a pod, and answers whether this call is the one that did it.
     *
     * <p>
     * A plain {@code save} would not do: Spring Data JDBC turns it into an UPDATE for a key that already exists, so
     * a redelivered event would look like a fresh placement and the count would be incremented twice. The insert
     * with {@code ON CONFLICT DO NOTHING} returns 0 rows affected on a redelivery, which is exactly the signal the
     * caller needs to skip the increment.
     * </p>
     *
     * @return 1 if the placement was newly recorded, 0 if it was already known
     */
    @Modifying
    @Query("""
            insert into pod_registry.pod_store_placement (store_id, pod_id, placed_at)
            values (:storeId, :podId, now())
            on conflict (store_id) do nothing""")
    int claim(String storeId, String podId);

    /**
     * Recounts a pod's stores from the placement table.
     *
     * <p>
     * Used to set {@code capacity_stores} rather than incrementing it, so the stored count is derived from rows
     * that exist rather than from a running total that can only drift.
     * </p>
     */
    @Modifying
    @Query("""
            update pod_registry.pod
            set capacity_stores = (select count(*) from pod_registry.pod_store_placement where pod_id = :podId)
            where id = :podId""")
    int recountCapacity(String podId);

}
