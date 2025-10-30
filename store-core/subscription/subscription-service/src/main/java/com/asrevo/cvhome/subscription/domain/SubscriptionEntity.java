package com.asrevo.cvhome.subscription.domain;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.SubscriptionPlan;
import com.asrevo.cvhome.subscription.commons.RecurringPlan;
import com.asrevo.cvhome.subscription.commons.SubscriptionStatus;
import com.asrevo.cvhome.subscription.commons.event.SubscriptionActivateEvent;
import com.asrevo.cvhome.subscription.commons.event.SubscriptionCreatedEvent;
import com.asrevo.cvhome.subscription.commons.event.SubscriptionDeActivateEvent;
import java.time.Instant;
import lombok.Getter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("subscription")
public class SubscriptionEntity extends BaseEntity<SubscriptionEntity, ManagerOrgId> {

	@Column("created_date")
	private Instant createdDate;

	@Column("last_renewed_date")
	private Instant lastRenewedDate;

	@Column("end_date")
	private Instant endDate;

	@Column("de_activated_date")
	private Instant deActivatedDate;

	@Column("subscription_plan")
	private SubscriptionPlan subscriptionPlan;

	@Column("subscription_status")
	private SubscriptionStatus status;

	@Column("recurring_plan")
	private RecurringPlan recurringPlan;

	public static SubscriptionEntity createInitialSubscription(ManagerOrgId orgId) {
		SubscriptionEntity entity = new SubscriptionEntity();
		entity.setId(orgId);
		Instant now = Instant.now();
		entity.createdDate = now;
		entity.lastRenewedDate = now;
		SubscriptionPlan defaultSubscriptionPlan = SubscriptionPlan.FREE;
		entity.recurringPlan = RecurringPlan.MONTH;
		entity.endDate = Instant.now().plus(defaultSubscriptionPlan.getTrialAmount());
		entity.deActivatedDate = null;
		entity.subscriptionPlan = defaultSubscriptionPlan;
		entity.status = SubscriptionStatus.ACTIVE;
		entity.registerEvent(SubscriptionCreatedEvent.from(entity.id, entity.subscriptionPlan));
		return entity;
	}

	public SubscriptionEntity deActivate() {
		if (SubscriptionStatus.ACTIVE.equals(this.status)) {
			this.deActivatedDate = Instant.now();
			this.status = SubscriptionStatus.NOT_ACTIVE;
			this.registerEvent(SubscriptionDeActivateEvent.from(this.id));
		}
		return this;
	}

	public SubscriptionEntity renew(SubscriptionPlan subscriptionPlan, Instant startDate, Instant endDate,
			RecurringPlan recurringPlan) {
		this.deActivatedDate = null;
		this.subscriptionPlan = subscriptionPlan;
		this.lastRenewedDate = startDate;
		this.endDate = endDate;
		this.status = SubscriptionStatus.ACTIVE;
		this.recurringPlan = recurringPlan;
		this.registerEvent(SubscriptionActivateEvent.from(this.id));
		return this;
	}

	@Override
	protected ManagerOrgId generateId() {
		return id;
	}

}
