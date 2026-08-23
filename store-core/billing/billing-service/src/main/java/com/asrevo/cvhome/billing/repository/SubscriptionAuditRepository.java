package com.asrevo.cvhome.billing.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import com.asrevo.cvhome.billing.commons.dto.admin.SubscriptionAuditView;
import com.asrevo.cvhome.billing.domain.SubscriptionAuditEntity;
import com.asrevo.cvhome.commons.domain.StatisticEntry;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * The subscription audit trail.
 *
 * <p>
 * Every optional filter below is written as {@code cast(:x as varchar) is null or col = :x}, the idiom tenancy uses
 * for the same job: Postgres cannot infer the type of a bare null parameter, and without the cast a filter left
 * unset is a bind error rather than a filter left unset.
 * </p>
 */
public interface SubscriptionAuditRepository extends CrudRepository<SubscriptionAuditEntity, Long> {

    List<SubscriptionAuditEntity> findAllByStoreIdOrderByOccurredAtDesc(StoreMerchantId storeId);

    /**
     * One page of the trail, newest first.
     *
     * <p>
     * <strong>Ordered by {@code occurred_at desc, id desc}.</strong> {@code occurred_at} is {@code Instant.now()} at
     * write time and two rows written in one transaction can share it to the microsecond; the {@code bigserial} is
     * what makes the order total, and therefore what stops a row appearing on two pages or on neither.
     * </p>
     *
     * <p>
     * The two left joins resolve {@code from_plan_id} and {@code to_plan_id} to the codes the screen shows. Doing it
     * here rather than in the service is the difference between one hash join over a handful of catalogue rows and
     * two uncached primary-key reads per row of the page.
     * </p>
     */
    @Query("""
            select a.id                    as id,
                   a.store_id              as store,
                   a.org_id                as org,
                   a.event_type            as event_type,
                   a.from_status           as from_status,
                   a.to_status             as to_status,
                   fp.code                 as from_plan_code,
                   tp.code                 as to_plan_code,
                   a.source                as source,
                   a.actor                 as actor,
                   a.stripe_event_id       as stripe_event_id,
                   a.detail                as detail,
                   a.occurred_at           as occurred_at
            from billing.subscription_audit a
            left join billing.plan fp on fp.id = a.from_plan_id
            left join billing.plan tp on tp.id = a.to_plan_id
            where (cast(:store as varchar) is null or a.store_id = :store)
              and (cast(:org as varchar) is null or a.org_id = :org)
              and (cast(:eventType as varchar) is null or a.event_type = :eventType)
              and (cast(:source as varchar) is null or a.source = :source)
              and (cast(:from as timestamp) is null or a.occurred_at >= :from)
              and (cast(:to as timestamp) is null or a.occurred_at < :to)
            order by a.occurred_at desc, a.id desc
            limit :limit offset :offset""")
    List<SubscriptionAuditView> findVisible(String store, String org, String eventType, String source,
                                            Instant from, Instant to, int limit, long offset);

    /**
     * The matching total. A separate query because Spring Data JDBC has no {@code countQuery} attribute — that is
     * JPA's — so the page is assembled by hand, exactly as {@code InternalOrgServiceImpl} does it.
     */
    @Query("""
            select count(*) from billing.subscription_audit a
            where (cast(:store as varchar) is null or a.store_id = :store)
              and (cast(:org as varchar) is null or a.org_id = :org)
              and (cast(:eventType as varchar) is null or a.event_type = :eventType)
              and (cast(:source as varchar) is null or a.source = :source)
              and (cast(:from as timestamp) is null or a.occurred_at >= :from)
              and (cast(:to as timestamp) is null or a.occurred_at < :to)""")
    long countVisible(String store, String org, String eventType, String source, Instant from, Instant to);

    /**
     * Subscriptions <em>started</em> per day, by plan code.
     *
     * <p>
     * <strong>Read from the audit trail rather than from {@code store_subscription.created_date}</strong>, and the
     * difference is the whole meaning of the number. A {@code store_subscription} row is written by provisioning the
     * moment a store is created, so {@code created_date} counts every store that ever entered billing including the
     * ones that never paid — which is already what {@code store-statistic} answers. "Started" means started paying
     * or trialling, and this table is the only thing that records when that happened. Both tables begin at the same
     * moment in history, so nothing is lost by asking this one.
     * </p>
     *
     * <p>
     * <strong>{@code distinct on (store_id)}, because {@code ACTIVATED} fires more than once.</strong> A suspended
     * store that pays and comes back activates again, and counting raw rows would book a returning customer as a new
     * one. The range filter sits <em>outside</em> the sub-select on purpose: pushing it in would pick the first row
     * in the window rather than the first ever, which is the same double-count with extra steps.
     * </p>
     *
     * <p>
     * {@code coalesce} to {@code UNKNOWN} rather than dropping the row — a dangling {@code to_plan_id} after a
     * catalogue change should show up as a visible bar, not as a total that quietly shrank.
     * </p>
     *
     * <p>
     * {@code date()} resolves in the <em>database session's</em> timezone. The service runs UTC, so a payment at
     * 23:50 local lands on the previous day for an operator elsewhere. Doing it properly means an
     * {@code AT TIME ZONE} parameter on this query and on the revenue one, which is its own change.
     * </p>
     */
    @Query("""
            select cast(date(s.occurred_at) as varchar) as date,
                   coalesce(p.code, 'UNKNOWN')          as name,
                   count(*)                             as value
            from (select distinct on (a.store_id) a.store_id, a.occurred_at, a.to_plan_id
                  from billing.subscription_audit a
                  where a.event_type in ('TRIAL_STARTED', 'ACTIVATED')
                  order by a.store_id, a.occurred_at) s
            left join billing.plan p on p.id = s.to_plan_id
            where s.occurred_at >= :from and s.occurred_at < :to
            group by date(s.occurred_at), coalesce(p.code, 'UNKNOWN')
            order by 1, 2""")
    List<StatisticEntry> subscriptionStatistic(Instant from, Instant to);

}
