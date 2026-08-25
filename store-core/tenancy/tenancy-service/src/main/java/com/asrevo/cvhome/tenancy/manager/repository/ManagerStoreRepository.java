package com.asrevo.cvhome.tenancy.manager.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import com.asrevo.cvhome.commons.domain.StatisticEntry;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.tenancy.commons.dto.PodStoreCount;
import com.asrevo.cvhome.tenancy.manager.entity.ManagerStoreEntity;

public interface ManagerStoreRepository
        extends CrudRepository<ManagerStoreEntity, StoreMerchantId>, QueryByExampleExecutor<ManagerStoreEntity> {

    Boolean existsByName(String name);

    /**
     * The stores a caller may see, excluding soft-deleted ones.
     *
     * <p>
     * Replaces Query-by-Example, which could express the optional filters but not "and not DELETED" — so a
     * deleted store went on appearing in the console, which is a strange thing for delete to do. The casts are
     * needed because Postgres cannot infer the type of a bare null parameter.
     * </p>
     *
     * @param orgId   null for a platform-wide caller: a super admin or a store-core service principal
     * @param storeId null unless the caller is confined to one store
     * @param name    a search term, matched case-insensitively as a <em>substring</em> of the store's name or its
     *                id. It was an equality on the name alone, which is a lookup rather than a search — nothing
     *                passed it, and the console's first search box over this list is what needed it to be one
     * @param podId   null unless the caller asked for one pod's stores. It narrows what the caller may already
     *                see rather than widening it, so it composes with the two scoping parameters above
     */
    @Query("""
            select * from tenancy.manager_store where status <> 'DELETED'
              and (cast(:orgId as varchar) is null or org_id = :orgId)
              and (cast(:storeId as varchar) is null or id = :storeId)
              and (cast(:name as varchar) is null
                   or name ilike '%' || :name || '%'
                   or id ilike '%' || :name || '%')
              and (cast(:podId as varchar) is null or pod_id = :podId)
            order by created_date limit :limit offset :offset""")
    List<ManagerStoreEntity> findVisible(String orgId, String storeId, String name, String podId, int limit,
                                         long offset);

    /** The matching total, for the page the query above returns a slice of. */
    @Query("""
            select count(*) from tenancy.manager_store where status <> 'DELETED'
              and (cast(:orgId as varchar) is null or org_id = :orgId)
              and (cast(:storeId as varchar) is null or id = :storeId)
              and (cast(:name as varchar) is null
                   or name ilike '%' || :name || '%'
                   or id ilike '%' || :name || '%')
              and (cast(:podId as varchar) is null or pod_id = :podId)""")
    long countVisible(String orgId, String storeId, String name, String podId);

    /**
     * How many stores sit on each pod, platform-wide.
     *
     * <p>
     * Tenancy owns {@code manager_store.pod_id}, so this is the authoritative count. Pod-registry's
     * {@code pod.capacity_stores} is a mirror it maintains from tenancy's outbox, and knows only about stores
     * placed through it.
     * </p>
     *
     * <p>
     * One query rather than one per pod: the fleet table wants every count at once, and a column that costs a
     * request per row is not a column. Pods with no stores are <em>absent</em> rather than zero — {@code group by}
     * has nothing to group — so a caller must read a missing pod as none placed.
     * </p>
     */
    @Query("""
            select pod_id, count(*) as stores from tenancy.manager_store
            where status <> 'DELETED' and pod_id is not null
            group by pod_id""")
    List<PodStoreCount> storesPerPod();

    /**
     * Every store tenancy considers placed on a pod — what the startup reconciliation replays to the registry.
     *
     * <p>
     * Deliberately not filtered to {@code SUCCESSFULLY_PROVISIONING}: a store that is still building, or that
     * failed to build, still occupies the pod it was placed on, and the registry's capacity count is about
     * occupancy rather than health. `DELETED` is excluded because those rows are gone as far as the platform is
     * concerned, and counting them would keep a retired store consuming capacity forever.
     * </p>
     */
    @Query("""
            select * from tenancy.manager_store
            where status <> 'DELETED' and pod_id is not null""")
    List<ManagerStoreEntity> findPlaced();

    /**
     * Stores that entered provisioning and never left it.
     *
     * <p>
     * There is no "provisioning started at" column, so this leans on {@code created_date}: a store still
     * IN_PROGRESS long after it was created is stranded regardless of exactly when the attempt began. Imprecise
     * in the same direction as the generous threshold it is used with, which is the safe direction — it reaps
     * late rather than early, and the reaper resets rather than re-creates.
     * </p>
     */
    @Query("""
            select * from tenancy.manager_store
            where provisioning_state = 'IN_PROGRESS_PROVISIONING' and created_date < :cutoff""")
    List<ManagerStoreEntity> findStuckInProvisioning(Instant cutoff);

    /**
     * The schema qualifier here was left as {@code manager.} by the rename, so this query referenced a schema that
     * no longer exists and would have failed the first time the statistics screen was opened.
     */
    @Query("""
             select date(m.created_date) as date,count(date(m.created_date)) as value
             from tenancy.manager_store m
             where m.created_date between  :from and :to
            group by date(m.created_date)""")
    List<StatisticEntry> storeStatistic(Instant from, Instant to);

}
