package com.asrevo.cvhome.billing.repository;

import java.time.Instant;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.asrevo.cvhome.billing.domain.OrgTrialGrantEntity;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;

public interface OrgTrialGrantRepository extends CrudRepository<OrgTrialGrantEntity, ManagerOrgId> {

    /**
     * Claims the org's one trial, atomically.
     *
     * <p>
     * Three deliberate choices, each of which was wrong in an earlier draft:
     * </p>
     *
     * <ul>
     * <li>A literal {@code INSERT} rather than {@code save}. {@code save} on an aggregate that already exists becomes
     * an {@code UPDATE}, which would hand a second trial to an org that had already spent one.</li>
     * <li>{@code ON CONFLICT DO NOTHING} rather than letting the primary key raise. On Postgres a constraint
     * violation aborts the enclosing transaction, so catching the exception and carrying on does not work — every
     * later statement fails with "current transaction is aborted". This returns 0 instead, leaving the transaction
     * usable so the caller can go on to create an unpaid subscription.</li>
     * <li>The conflict target is the primary key, so two concurrent first-store creations for one org still resolve
     * to exactly one winner. That race is the entire rule; there is no read-then-write to lose it.</li>
     * </ul>
     *
     * @return 1 when this call claimed the trial, 0 when the org had already spent it
     */
    @Modifying
    @Query("""
            insert into billing.org_trial_grant (org_id, store_id, granted_at, trial_end, version)
            values (:orgId, :storeId, :grantedAt, :trialEnd, 0)
            on conflict (org_id) do nothing
            """)
    int claim(@Param("orgId") String orgId, @Param("storeId") String storeId,
              @Param("grantedAt") Instant grantedAt, @Param("trialEnd") Instant trialEnd);

}
