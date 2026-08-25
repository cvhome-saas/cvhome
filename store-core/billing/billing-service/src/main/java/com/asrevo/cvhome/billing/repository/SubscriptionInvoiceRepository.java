package com.asrevo.cvhome.billing.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.asrevo.cvhome.billing.commons.StripeInvoiceId;
import com.asrevo.cvhome.billing.domain.SubscriptionInvoiceEntity;
import com.asrevo.cvhome.billing.repository.projection.InvoiceTotalRow;
import com.asrevo.cvhome.billing.repository.projection.PlatformInvoiceRow;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StatisticEntry;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface SubscriptionInvoiceRepository extends CrudRepository<SubscriptionInvoiceEntity, StripeInvoiceId>,
        PagingAndSortingRepository<SubscriptionInvoiceEntity, StripeInvoiceId> {

    /**
     * A store's invoices, newest first, scoped to the org that owns it.
     *
     * <p>
     * The org is part of the query rather than checked afterwards, for the same reason as the subscription read: the
     * shared permission checker cannot tell which org a store belongs to, so the boundary has to be in the SQL.
     * </p>
     */
    Page<SubscriptionInvoiceEntity> findAllByStoreIdAndOrgIdOrderByIssuedAtDesc(StoreMerchantId storeId,
                                                                                ManagerOrgId orgId,
                                                                                Pageable pageable);

    Page<SubscriptionInvoiceEntity> findAllByStoreIdOrderByIssuedAtDesc(StoreMerchantId storeId, Pageable pageable);

    List<SubscriptionInvoiceEntity> findAllByStoreId(StoreMerchantId storeId);

    /**
     * One page of the platform's invoice ledger, newest first.
     *
     * <p>
     * The date range is over {@code issued_at}: an operator filtering the ledger asks "what did we bill in August",
     * and an unpaid invoice has no {@code paid_at} to be found by. {@link #revenueStatistic} makes the opposite
     * choice for the opposite reason.
     * </p>
     */
    @Query("""
            select i.id                 as id,
                   i.store_id           as store,
                   i.org_id             as org,
                   i.invoice_number     as number,
                   i.status             as status,
                   i.currency           as currency,
                   i.amount_due         as amount_due,
                   i.amount_paid        as amount_paid,
                   i.period_start       as period_start,
                   i.period_end         as period_end,
                   i.issued_at          as issued_at,
                   i.paid_at            as paid_at,
                   i.hosted_invoice_url as hosted_invoice_url,
                   i.invoice_pdf_url    as invoice_pdf_url
            from billing.subscription_invoice i
            where (cast(:org as varchar) is null or i.org_id = :org)
              and (cast(:store as varchar) is null or i.store_id = :store)
              and (cast(:status as varchar) is null or i.status = :status)
              and (cast(:from as timestamp) is null or i.issued_at >= :from)
              and (cast(:to as timestamp) is null or i.issued_at < :to)
            order by i.issued_at desc, i.id desc
            limit :limit offset :offset""")
    List<PlatformInvoiceRow> findVisible(String org, String store, String status, Instant from, Instant to,
                                         int limit, long offset);

    /** The matching total. Spring Data JDBC has no {@code countQuery}, so the page is assembled by hand. */
    @Query("""
            select count(*) from billing.subscription_invoice i
            where (cast(:org as varchar) is null or i.org_id = :org)
              and (cast(:store as varchar) is null or i.store_id = :store)
              and (cast(:status as varchar) is null or i.status = :status)
              and (cast(:from as timestamp) is null or i.issued_at >= :from)
              and (cast(:to as timestamp) is null or i.issued_at < :to)""")
    long countVisible(String org, String store, String status, Instant from, Instant to);

    /**
     * What the current filter comes to, one row per currency.
     *
     * <p>
     * <strong>Grouped by currency and never summed across it.</strong> Nothing on this platform holds an exchange
     * rate, so a mixed total is a wrong number rather than a missing one.
     * </p>
     *
     * <p>
     * A second call on the same body rather than a field on the page: the ledger's rows can render while the sums
     * are still being computed, and a response that is sometimes a page and sometimes a page-plus-envelope is the
     * shape the console's {@code SpringPage} type exists to avoid.
     * </p>
     */
    @Query("""
            select i.currency         as currency,
                   sum(i.amount_paid) as paid,
                   sum(i.amount_due)  as due,
                   count(*)           as invoices
            from billing.subscription_invoice i
            where (cast(:org as varchar) is null or i.org_id = :org)
              and (cast(:store as varchar) is null or i.store_id = :store)
              and (cast(:status as varchar) is null or i.status = :status)
              and (cast(:from as timestamp) is null or i.issued_at >= :from)
              and (cast(:to as timestamp) is null or i.issued_at < :to)
            group by i.currency
            order by i.currency""")
    List<InvoiceTotalRow> totals(String org, String store, String status, Instant from, Instant to);

    /**
     * Money actually collected, per day and per currency.
     *
     * <p>
     * <strong>Keyed on {@code paid_at}, not {@code issued_at}, and the reason is specific to this table.</strong>
     * {@code SubscriptionInvoiceEntity.settled} writes {@code status}, {@code amount_paid} and {@code paid_at} and
     * does not touch {@code issued_at}. Sum on {@code issued_at} and a <em>past</em> day's bar moves when a late
     * payment lands: an operator reloads last month and the chart has changed under them. Keyed on {@code paid_at},
     * a day's figure changes only when money actually moved on that day.
     * </p>
     *
     * <p>
     * {@code UNCOLLECTIBLE} and {@code VOID} fall out of the same predicate — they are not {@code PAID} — so nothing
     * written off is counted as revenue. Amounts are <strong>minor units</strong>, and the currency is in the
     * grouping key rather than converted.
     * </p>
     *
     * <p>
     * {@code date()} resolves in the database session's timezone; see {@code SubscriptionAuditRepository
     * .subscriptionStatistic} for what that costs and why it is not fixed here.
     * </p>
     */
    @Query("""
            select cast(date(i.paid_at) as varchar) as date,
                   i.currency                       as name,
                   sum(i.amount_paid)               as value
            from billing.subscription_invoice i
            where i.status = 'PAID' and i.paid_at >= :from and i.paid_at < :to
            group by date(i.paid_at), i.currency
            order by 1, 2""")
    List<StatisticEntry> revenueStatistic(Instant from, Instant to);

}
