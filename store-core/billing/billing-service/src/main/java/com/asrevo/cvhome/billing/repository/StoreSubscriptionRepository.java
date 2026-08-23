package com.asrevo.cvhome.billing.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.asrevo.cvhome.billing.commons.StripeCustomerId;
import com.asrevo.cvhome.billing.commons.StripeSubscriptionId;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.commons.dto.admin.PlanSubscriptionCount;
import com.asrevo.cvhome.billing.domain.StoreSubscriptionEntity;
import com.asrevo.cvhome.billing.repository.projection.PlanRecurringValueRow;
import com.asrevo.cvhome.billing.repository.projection.PlatformSubscriptionRow;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface StoreSubscriptionRepository extends CrudRepository<StoreSubscriptionEntity, StoreMerchantId> {

    List<StoreSubscriptionEntity> findAllByOrgId(ManagerOrgId orgId);

    /**
     * Loads a store's subscription only if it belongs to {@code orgId}.
     *
     * <p>
     * The tenant boundary, enforced in the query rather than trusted to the permission layer.
     * {@code StoreRoleAccessChecker.isOrgAdmin} currently returns true for any store once the caller holds the
     * org-admin role — it has no way to map a store to its org, and says so in a {@code TODO}. Billing does have that
     * mapping, in {@code org_id} on this very row, so it uses it. Without this, one org's admin could read another
     * org's plan, spend and invoices.
     * </p>
     */
    Optional<StoreSubscriptionEntity> findByIdAndOrgId(StoreMerchantId id, ManagerOrgId orgId);

    int countByOrgIdAndStatus(ManagerOrgId orgId, SubscriptionStatus status);

    Optional<StoreSubscriptionEntity> findByStripeSubscriptionId(StripeSubscriptionId stripeSubscriptionId);

    /**
     * Whether this provider customer is one of ours — used to tell an invoice we cannot attribute <em>yet</em> from
     * one that was never ours to begin with.
     */
    boolean existsByStripeCustomerId(StripeCustomerId stripeCustomerId);

    Optional<StoreSubscriptionEntity> findFirstByOrgIdAndStripeCustomerIdNotNull(ManagerOrgId orgId);

    List<StoreSubscriptionEntity> findAllByStatusAndTrialEndBefore(SubscriptionStatus status, Instant before);

    List<StoreSubscriptionEntity> findAllByStatusAndGraceUntilBefore(SubscriptionStatus status, Instant before);

    List<StoreSubscriptionEntity> findAllByPendingEffectiveAtBefore(Instant before);

    /**
     * The stores no enforcement layer should let through. Kept as a projection of ids rather than whole rows: the
     * gateway polls this once a minute for every blocked store on the platform.
     */
    List<StoreSubscriptionEntity> findAllByStatusIn(List<SubscriptionStatus> statuses);

    /**
     * Loads several subscriptions by store id.
     *
     * <p>
     * Written out rather than using {@code findAllById}, which renders each element of the {@code IN} list as its own
     * parenthesised group — {@code IN ((?), (?))} — because the id is a value object Spring Data treats as
     * potentially composite. Postgres rejects that as a syntax error, so the derived method fails for every
     * multi-store read.
     * </p>
     */
    @Query("select * from billing.store_subscription where id in (:ids)")
    List<StoreSubscriptionEntity> findAllByStoreIds(@Param("ids") Collection<String> ids);

    /**
     * @return the customer this org already has at the provider, if any of its stores has one
     */
    default Optional<StripeCustomerId> findCustomerOf(ManagerOrgId orgId) {
        return findFirstByOrgIdAndStripeCustomerIdNotNull(orgId)
                .map(StoreSubscriptionEntity::getStripeCustomerId);
    }

    /**
     * One page of the platform's subscription register, joined to the plan and the price in force.
     *
     * <p>
     * <strong>{@code blockedOnly} is a named boolean rather than a status list</strong>, and the reason is both
     * shapes at once: "blocked" is <em>three</em> statuses — the same {@code PENDING, SUSPENDED, CANCELED} that
     * {@code EntitlementServiceImpl.BLOCKED} enforces — and a nullable list cannot be written with the
     * {@code cast(:x as varchar) is null} idiom the other filters use. It is also the thing an operator actually
     * asks for, so naming it beats making the console spell it out.
     * </p>
     *
     * <p>
     * This is what replaces {@code entitlement/private/blocked-stores} for a human. That endpoint answers a bare
     * list of ids — no reason, no since-when — and sits on the gateway's once-a-minute hot path behind a token that
     * also opens {@code ExternalStoreQuotaApi.provision}, which creates a subscription and spends the org's one
     * trial through a primary key that can never be reclaimed. Widening that token for a report would be a one-way
     * door reached through what reads like a read. This query answers the same question strictly better and is
     * gated on {@code ROLE_SUPER_ADMIN} instead.
     * </p>
     *
     * <p>
     * The joins are {@code left}: a {@code PENDING} store has no plan, and an inner join would hide exactly the rows
     * the blocked filter exists to surface.
     * </p>
     */
    @Query("""
            select s.id                   as store,
                   s.org_id               as org,
                   s.status               as status,
                   p.code                 as plan_code,
                   p.display_name         as plan_display_name,
                   pp.currency            as currency,
                   pp.unit_amount         as unit_amount,
                   s.current_period_end   as current_period_end,
                   s.trial_end            as trial_end,
                   s.grace_until          as grace_until,
                   s.suspended_at         as suspended_at,
                   s.canceled_at          as canceled_at,
                   s.cancel_at_period_end as cancel_at_period_end,
                   (s.stripe_subscription_id is not null) as provider_linked,
                   s.created_date         as created_date
            from billing.store_subscription s
            left join billing.plan p on p.id = s.plan_id
            left join billing.plan_price pp on pp.id = s.plan_price_id
            where (cast(:org as varchar) is null or s.org_id = :org)
              and (cast(:status as varchar) is null or s.status = :status)
              and (cast(:planCode as varchar) is null or p.code = :planCode)
              and (cast(:term as varchar) is null or s.id ilike '%' || :term || '%')
              and (:blockedOnly = false or s.status in ('PENDING', 'SUSPENDED', 'CANCELED'))
            order by s.created_date desc, s.id desc
            limit :limit offset :offset""")
    List<PlatformSubscriptionRow> findVisible(String org, String status, String planCode, String term,
                                              boolean blockedOnly, int limit, long offset);

    /** The matching total. Spring Data JDBC has no {@code countQuery}, so the page is assembled by hand. */
    @Query("""
            select count(*)
            from billing.store_subscription s
            left join billing.plan p on p.id = s.plan_id
            where (cast(:org as varchar) is null or s.org_id = :org)
              and (cast(:status as varchar) is null or s.status = :status)
              and (cast(:planCode as varchar) is null or p.code = :planCode)
              and (cast(:term as varchar) is null or s.id ilike '%' || :term || '%')
              and (:blockedOnly = false or s.status in ('PENDING', 'SUSPENDED', 'CANCELED'))""")
    long countVisible(String org, String status, String planCode, String term, boolean blockedOnly);

    /**
     * Who is on what, right now — one row per plan per lifecycle state.
     *
     * <p>
     * The join is {@code left} so the plan-less {@code PENDING} stores stay visible as a row with a null code. An
     * inner join would answer a smaller, tidier and wrong number.
     * </p>
     */
    @Query("""
            select p.code         as plan_code,
                   p.display_name as plan_display_name,
                   p.tier         as tier,
                   s.status       as status,
                   count(*)       as subscriptions
            from billing.store_subscription s
            left join billing.plan p on p.id = s.plan_id
            group by p.code, p.display_name, p.tier, s.status
            order by p.tier nulls first, p.code, s.status""")
    List<PlanSubscriptionCount> planSubscriptionCounts();

    /**
     * The annualised run rate, per plan per currency per state.
     *
     * <p>
     * <strong>Annualised in SQL, divided once in Java.</strong> A yearly price divided by twelve truncates on every
     * row; a monthly one multiplied by twelve is exact in {@code bigint}. So the sum is built at the annual scale
     * and the monthly figure is derived from the aggregate rather than from each row.
     * </p>
     *
     * <p>
     * A price is required to have a value here, so the join is inner: a subscription with no {@code plan_price_id}
     * is contracted for nothing and contributes nothing. It is still counted by {@link #planSubscriptionCounts()},
     * which is the query that answers "how many".
     * </p>
     */
    @Query("""
            select p.code      as plan_code,
                   s.status    as status,
                   pp.currency as currency,
                   count(*)    as subscriptions,
                   sum(case pp.billing_interval
                           when 'YEAR' then pp.unit_amount
                           else pp.unit_amount * 12 end) as annual
            from billing.store_subscription s
            join billing.plan p on p.id = s.plan_id
            join billing.plan_price pp on pp.id = s.plan_price_id
            group by p.code, s.status, pp.currency
            order by p.code, s.status, pp.currency""")
    List<PlanRecurringValueRow> planRecurringValue();

}
