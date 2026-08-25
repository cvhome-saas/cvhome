package com.asrevo.cvhome.tenancy.manager.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StatisticEntry;
import com.asrevo.cvhome.tenancy.manager.entity.ManagerOrgEntity;

public interface ManagerOrgRepository extends CrudRepository<ManagerOrgEntity, ManagerOrgId>,
        QueryByExampleExecutor<ManagerOrgEntity>, PagingAndSortingRepository<ManagerOrgEntity, ManagerOrgId> {


    @Query("""
             select date(m.created_date) as date,count(date(m.created_date)) as value
             from tenancy.manager_org m
             where m.created_date between  :from and :to
            group by date(m.created_date)""")
    List<StatisticEntry> orgStatistic(Instant from, Instant to);

    /**
     * One page of organizations, narrowed by a search term and a status.
     *
     * <p>
     * The term spans the name and the contact email in one predicate rather than taking a parameter each. Almost
     * every organization on the platform is unnamed — {@code createOrgFromUser} sets no name — so the console shows
     * the email where the name is missing, and a box that searched only one of the two would fail to find exactly
     * the rows it is displaying by the other.
     * </p>
     *
     * <p>
     * {@code ilike} rather than {@code =}: a search box that requires the whole value is a lookup, not a search.
     * The casts are needed because Postgres cannot infer the type of a bare null parameter.
     * </p>
     */
    @Query("""
            select * from tenancy.manager_org
            where (cast(:term as varchar) is null
                   or name ilike '%' || :term || '%'
                   or email ilike '%' || :term || '%')
              and (cast(:status as varchar) is null or status = :status)
            order by created_date desc limit :limit offset :offset""")
    List<ManagerOrgEntity> findVisible(String term, String status, int limit, long offset);

    /** The matching total, for the page the query above returns a slice of. */
    @Query("""
            select count(*) from tenancy.manager_org
            where (cast(:term as varchar) is null
                   or name ilike '%' || :term || '%'
                   or email ilike '%' || :term || '%')
              and (cast(:status as varchar) is null or status = :status)""")
    long countVisible(String term, String status);

    /**
     * Organizations with no recorded owner — what the one-time backfill works through.
     *
     * <p>
     * Expected to be every row on a platform upgraded to this release and none on one created after it, because
     * {@code SignupServiceImpl} now writes the column at signup. It is therefore also the query that says whether
     * the backfill still has anything to do, which is why it is a query rather than a filter over {@code findAll}.
     * </p>
     */
    @Query("select * from tenancy.manager_org m where m.owner_user_id is null")
    List<ManagerOrgEntity> findWithoutOwner();

}
