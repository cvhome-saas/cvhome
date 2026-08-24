package com.asrevo.cvhome.billing.service;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.asrevo.cvhome.billing.commons.AuditEventType;
import com.asrevo.cvhome.billing.commons.ChangeSource;
import com.asrevo.cvhome.billing.commons.PlanId;
import com.asrevo.cvhome.billing.commons.StripeEventId;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.domain.StoreSubscriptionEntity;
import com.asrevo.cvhome.billing.domain.SubscriptionAuditEntity;
import com.asrevo.cvhome.billing.repository.SubscriptionAuditRepository;
import com.asrevo.cvhome.billing.service.impl.SubscriptionAuditServiceImpl;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * What an audit row actually records.
 *
 * <p>
 * Two columns existed and were written as literal nulls. {@code actor} was null on every {@code ChangeSource.API}
 * row on the platform — the table recorded <em>that</em> a person changed a plan and never <em>which</em> person,
 * which is precisely the question a billing dispute turns on. And {@code from_plan_id} was passed as {@code null} at
 * both writers, so a {@code PLAN_UPGRADED} row said which plan the store landed on and not which one it left.
 * </p>
 *
 * <p>
 * Neither failed anything. A null in a nullable column is indistinguishable from a fact nobody had; these tests are
 * what make the columns' emptiness a failure instead.
 * </p>
 */
class SubscriptionAuditActorTest {

    private static final ManagerOrgId ORG = new ManagerOrgId("21f023932bc66470c104b76f");

    private static final StoreMerchantId STORE = new StoreMerchantId("507f1f77bcf86cd799439011");

    private static final PlanId BASIC = new PlanId("607f1f77bcf86cd799439022");

    private static final PlanId PRO = new PlanId("607f1f77bcf86cd799439033");

    /** The signed-in operator these rows are supposed to name. */
    private static final String OPERATOR = "ops@cvhome.test";

    private static final String JOB_ACTOR = "billing-job";

    private static final StripeEventId EVENT = new StripeEventId("evt_1");

    private SubscriptionAuditRepository repository;

    private SubscriptionAuditServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(SubscriptionAuditRepository.class);
        when(repository.save(any(SubscriptionAuditEntity.class))).thenAnswer(call -> call.getArgument(0));
        service = new SubscriptionAuditServiceImpl(repository);
    }

    @Test
    @DisplayName("an API row names the authenticated principal")
    void anApiRowNamesThePrincipal() {
        service.record(SubscriptionStatus.ACTIVE, BASIC, onPlan(PRO), AuditEventType.PLAN_UPGRADED,
                ChangeSource.API, OPERATOR);

        SubscriptionAuditEntity row = saved();
        assertThat(row.getSource()).isEqualTo(ChangeSource.API);
        assertThat(row.getActor()).isEqualTo(OPERATOR);
    }

    @Test
    @DisplayName("a plan change records both the plan left and the plan landed on")
    void bothPlansAreRecorded() {
        service.record(SubscriptionStatus.ACTIVE, BASIC, onPlan(PRO), AuditEventType.PLAN_UPGRADED,
                ChangeSource.API, OPERATOR);

        SubscriptionAuditEntity row = saved();
        // The left half of the sentence, which was a literal null at this call site until now.
        assertThat(row.getFromPlanId()).isEqualTo(BASIC);
        assertThat(row.getToPlanId()).isEqualTo(PRO);
        assertThat(row.getFromStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        // The to-status is read off the saved entity rather than passed in, which is what makes a plan change that
        // did not move the lifecycle record the state it actually landed in.
        assertThat(row.getToStatus()).isEqualTo(SubscriptionStatus.TRIALING);
    }

    @Test
    @DisplayName("a webhook row still says stripe, and carries the event that caused it")
    void aWebhookRowStillSaysStripe() {
        service.recordFromWebhook(SubscriptionStatus.PAST_DUE, PRO, onPlan(PRO), AuditEventType.ACTIVATED,
                EVENT);

        SubscriptionAuditEntity row = saved();
        assertThat(row.getSource()).isEqualTo(ChangeSource.WEBHOOK);
        // Not the customer: the customer's act was the payment, and this is the provider saying what came of it.
        assertThat(row.getActor()).isEqualTo("stripe");
        assertThat(row.getStripeEventId()).isEqualTo(EVENT);
        assertThat(row.getFromPlanId()).isEqualTo(PRO);
    }

    @Test
    @DisplayName("a job row still names the job rather than a person")
    void aJobRowStillNamesTheJob() {
        service.record(SubscriptionStatus.PAST_DUE, PRO, onPlan(PRO), AuditEventType.SUSPENDED, ChangeSource.JOB,
                JOB_ACTOR);

        SubscriptionAuditEntity row = saved();
        assertThat(row.getSource()).isEqualTo(ChangeSource.JOB);
        // "The platform ended this" and "someone ended this" have to stay distinguishable.
        assertThat(row.getActor()).isEqualTo(JOB_ACTOR);
    }

    @Test
    @DisplayName("a row being created has no plan to have come from")
    void aCreatedRowHasNoFromPlan() {
        service.record(null, null, onPlan(BASIC), AuditEventType.TRIAL_STARTED, ChangeSource.SYSTEM, null);

        SubscriptionAuditEntity row = saved();
        assertThat(row.getFromStatus()).isNull();
        assertThat(row.getFromPlanId()).isNull();
        assertThat(row.getToPlanId()).isEqualTo(BASIC);
    }

    private SubscriptionAuditEntity saved() {
        ArgumentCaptor<SubscriptionAuditEntity> row = ArgumentCaptor.forClass(SubscriptionAuditEntity.class);
        verify(repository).save(row.capture());
        return row.getValue();
    }

    /**
     * A subscription sitting on one plan.
     *
     * <p>
     * Built through {@code trialing} and then moved, because the entity has no setters — which is also why the
     * before-plan has to be captured by the caller rather than read back off the row afterwards.
     * </p>
     */
    private static StoreSubscriptionEntity onPlan(PlanId plan) {
        return StoreSubscriptionEntity.trialing(STORE, ORG, plan, null, Instant.EPOCH.plusSeconds(86400));
    }

}
