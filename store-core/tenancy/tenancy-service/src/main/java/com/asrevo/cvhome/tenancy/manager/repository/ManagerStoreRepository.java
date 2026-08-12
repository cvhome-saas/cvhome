package com.asrevo.cvhome.tenancy.manager.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.StatisticEntry;
import com.asrevo.cvhome.tenancy.manager.entity.ManagerStoreEntity;

public interface ManagerStoreRepository
        extends CrudRepository<ManagerStoreEntity, ManagerStoreId>, QueryByExampleExecutor<ManagerStoreEntity> {

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
     */
    @Query("""
            select * from tenancy.manager_store where status <> 'DELETED'
              and (cast(:orgId as varchar) is null or org_id = :orgId)
              and (cast(:storeId as varchar) is null or id = :storeId)
              and (cast(:name as varchar) is null or name = :name)
            order by created_date limit :limit offset :offset""")
    List<ManagerStoreEntity> findVisible(String orgId, String storeId, String name, int limit, long offset);

    /** The matching total, for the page the query above returns a slice of. */
    @Query("""
            select count(*) from tenancy.manager_store where status <> 'DELETED'
              and (cast(:orgId as varchar) is null or org_id = :orgId)
              and (cast(:storeId as varchar) is null or id = :storeId)
              and (cast(:name as varchar) is null or name = :name)""")
    long countVisible(String orgId, String storeId, String name);

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
