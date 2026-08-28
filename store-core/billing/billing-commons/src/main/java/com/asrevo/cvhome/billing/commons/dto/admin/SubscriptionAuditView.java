package com.asrevo.cvhome.billing.commons.dto.admin;

import java.io.Serializable;
import java.time.Instant;

import com.asrevo.cvhome.billing.commons.AuditEventType;
import com.asrevo.cvhome.billing.commons.ChangeSource;
import com.asrevo.cvhome.billing.commons.StripeEventId;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * One line of the subscription audit trail, as the platform's Activity screen reads it.
 *
 * <p>
 * The plans arrive as <strong>codes rather than ids</strong>, resolved by two left joins in the query. Resolving
 * them in the service would mean one uncached catalogue read per column per row — a hundred round trips to fill two
 * columns of a fifty-row page — while {@code billing.plan} is a handful of rows that Postgres hash-joins for
 * nothing.
 * </p>
 *
 * <p>
 * Two honest nulls the console renders rather than guesses at. {@code actor} is null on every {@code API} row
 * written before the actor was threaded through {@code SubscriptionApi}; the table simply did not record who. And
 * for {@code PLAN_DOWNGRADE_SCHEDULED} the entity's plan has not moved yet, so {@code toPlanCode} is the plan the
 * store is <em>leaving</em> and the scheduled target appears nowhere — fixing that means changing what the event
 * records, not filling a null column, so it is left as it stands.
 * </p>
 *
 * @param detail free text; {@code SubscriptionAuditEntity.withDetail} has no callers anywhere, so it is null on
 *               every row on the platform and the console does not draw a column for it
 */
public record SubscriptionAuditView(Long id, StoreMerchantId store, ManagerOrgId org, AuditEventType eventType,
                                    SubscriptionStatus fromStatus, SubscriptionStatus toStatus, String fromPlanCode,
                                    String toPlanCode, ChangeSource source, String actor, StripeEventId stripeEventId,
                                    String detail, Instant occurredAt) implements Serializable {
}
