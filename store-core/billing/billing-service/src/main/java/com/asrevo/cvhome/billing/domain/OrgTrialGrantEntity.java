package com.asrevo.cvhome.billing.domain;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;

import lombok.Getter;

/**
 * The record that an org has spent its one trial, keyed by the org.
 *
 * <p>
 * The primary key <em>is</em> the rule. Two stores created for the same org at the same moment both attempt the
 * insert, the database admits exactly one, and the loser's store starts unpaid. Reading "has this org had a trial?"
 * and then writing would lose that race, and losing it means giving away free months.
 * </p>
 *
 * <p>
 * Not a {@code BaseEntity}: that would inherit an {@code @Id} mapped to a column called {@code id}, while the key
 * here is {@code org_id} — and it registers no events, so the aggregate-root machinery buys nothing. Rows are written
 * by {@code OrgTrialGrantRepository.claim}, never by {@code save}.
 * </p>
 */
@Getter
@Table(schema = "billing", name = "org_trial_grant")
public class OrgTrialGrantEntity {

    @Id
    @Column("org_id")
    private ManagerOrgId orgId;

    @Column("store_id")
    private ManagerStoreId storeId;

    @Column("granted_at")
    private Instant grantedAt;

    @Column("trial_end")
    private Instant trialEnd;

    @Version
    private Integer version;

}
