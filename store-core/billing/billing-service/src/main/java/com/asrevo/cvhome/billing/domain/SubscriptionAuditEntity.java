package com.asrevo.cvhome.billing.domain;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.asrevo.cvhome.billing.commons.AuditEventType;
import com.asrevo.cvhome.billing.commons.ChangeSource;
import com.asrevo.cvhome.billing.commons.PlanId;
import com.asrevo.cvhome.billing.commons.StripeEventId;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import lombok.Getter;

/**
 * One line of a subscription's history. Append-only: never updated, never deleted.
 *
 * <p>
 * Written explicitly by the service layer rather than by a listener, because there is no auditing framework available
 * here — the JPA one the pods use does not apply to Spring Data JDBC. Being explicit is also what lets a row record
 * <em>who</em> and <em>why</em>, which is the part that actually settles a billing dispute months later.
 * </p>
 *
 * <p>
 * Not a {@code BaseEntity}: the key is a database sequence, not one of the identifier value objects.
 * </p>
 */
@Getter
@Table(schema = "billing", name = "subscription_audit")
public class SubscriptionAuditEntity {

    @Id
    @Column("id")
    private Long id;

    @Column("store_id")
    private StoreMerchantId storeId;

    @Column("org_id")
    private ManagerOrgId orgId;

    @Column("event_type")
    private AuditEventType eventType;

    @Column("from_status")
    private SubscriptionStatus fromStatus;

    @Column("to_status")
    private SubscriptionStatus toStatus;

    @Column("from_plan_id")
    private PlanId fromPlanId;

    @Column("to_plan_id")
    private PlanId toPlanId;

    @Column("source")
    private ChangeSource source;

    @Column("actor")
    private String actor;

    @Column("stripe_event_id")
    private StripeEventId stripeEventId;

    @Column("detail")
    private String detail;

    @Column("occurred_at")
    private Instant occurredAt;

    @SuppressWarnings("java:S107")
    public static SubscriptionAuditEntity of(StoreMerchantId store, ManagerOrgId org, AuditEventType eventType,
                                             SubscriptionStatus fromStatus, SubscriptionStatus toStatus,
                                             PlanId fromPlan, PlanId toPlan, ChangeSource source, String actor) {
        SubscriptionAuditEntity entity = new SubscriptionAuditEntity();
        entity.storeId = store;
        entity.orgId = org;
        entity.eventType = eventType;
        entity.fromStatus = fromStatus;
        entity.toStatus = toStatus;
        entity.fromPlanId = fromPlan;
        entity.toPlanId = toPlan;
        entity.source = source;
        entity.actor = actor;
        entity.occurredAt = Instant.now();
        return entity;
    }

    public SubscriptionAuditEntity withDetail(String value) {
        this.detail = value;
        return this;
    }

    public SubscriptionAuditEntity causedBy(StripeEventId value) {
        this.stripeEventId = value;
        return this;
    }

}
