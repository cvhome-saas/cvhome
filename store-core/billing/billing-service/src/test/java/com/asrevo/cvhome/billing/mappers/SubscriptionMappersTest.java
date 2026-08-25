package com.asrevo.cvhome.billing.mappers;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.billing.commons.BillingInterval;
import com.asrevo.cvhome.billing.commons.EntitlementKey;
import com.asrevo.cvhome.billing.commons.EntitlementValue;
import com.asrevo.cvhome.billing.commons.PlanPriceId;
import com.asrevo.cvhome.billing.commons.StripeCustomerId;
import com.asrevo.cvhome.billing.commons.StripeSubscriptionId;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.commons.dto.EntitlementSnapshot;
import com.asrevo.cvhome.billing.commons.dto.SubscriptionView;
import com.asrevo.cvhome.billing.domain.PlanEntity;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;
import com.asrevo.cvhome.billing.domain.StoreSubscriptionEntity;
import com.asrevo.cvhome.billing.service.PlanCatalogService;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Rendering a subscription, which always needs the catalog because the row holds only identifiers.
 *
 * <p>
 * The interesting cases are the absent ones. A store that has never had a plan has to render as nulls rather than
 * fail, and its entitlement map has to be <em>empty</em> — which {@link EntitlementSnapshot} reads as unlimited, so
 * the status check is what has to gate such a store, and does.
 * </p>
 */
class SubscriptionMappersTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final ManagerOrgId ORG = new ManagerOrgId("32a034a43cd77581d105c87a");

    private static final Instant PERIOD_START = Instant.parse("2026-01-01T00:00:00Z");

    private static final Instant PERIOD_END = Instant.parse("2026-02-01T00:00:00Z");

    private PlanCatalogService catalog;

    private SubscriptionMappers mappers;

    private PlanEntity plan;

    private PlanPriceEntity price;

    @BeforeEach
    void setUp() {
        catalog = mock(PlanCatalogService.class);
        mappers = new SubscriptionMappers(catalog);
        plan = PlanEntity.create("PRO", "Pro", "For a store that is growing.", 20);
        price = PlanPriceEntity.create(plan.getId(), new CurrencyCode("USD"), 3000L, BillingInterval.MONTH, 0);
        when(catalog.findPlan(plan.getId())).thenReturn(Optional.of(plan));
        when(catalog.findPrice(price.getId())).thenReturn(Optional.of(price));
        when(catalog.entitlementsOf(plan.getId())).thenReturn(
                Map.of(EntitlementKey.MAX_PRODUCTS, EntitlementValue.limit(EntitlementKey.MAX_PRODUCTS, 500)));
    }

    private StoreSubscriptionEntity onPlan() throws Exception {
        return StoreSubscriptionEntity.pending(STORE, ORG)
                .activate(plan.getId(), price.getId(), PERIOD_START, PERIOD_END);
    }

    @Test
    @DisplayName("a subscription renders with its plan, price and grants resolved")
    void rendersAPlan() throws Exception {
        SubscriptionView view = mappers.toView(onPlan()
                .bindProvider(new StripeCustomerId("cus_1"), new StripeSubscriptionId("sub_1")));

        assertThat(view.store()).isEqualTo(STORE);
        assertThat(view.status()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(view.planCode()).isEqualTo("PRO");
        assertThat(view.planDisplayName()).isEqualTo("Pro");
        assertThat(view.amount().minorUnits()).isEqualTo(3000L);
        assertThat(view.currentPeriodEnd()).isEqualTo(PERIOD_END);
        assertThat(view.providerLinked()).isTrue();
        assertThat(view.entitlements()).containsKey(EntitlementKey.MAX_PRODUCTS);
    }

    @Test
    @DisplayName("a store that never bought anything renders as nulls rather than failing")
    void rendersAPlanlessStore() {
        SubscriptionView view = mappers.toView(StoreSubscriptionEntity.pending(STORE, ORG));

        assertThat(view.planCode()).isNull();
        assertThat(view.planDisplayName()).isNull();
        assertThat(view.amount()).isNull();
        assertThat(view.providerLinked()).isFalse();
        // Empty rather than absent-and-therefore-unlimited by accident: EntitlementSnapshot reads a missing key as
        // unlimited, so a plan-less store's gate has to be its status, which operable() answers false for.
        assertThat(view.entitlements()).isEmpty();
    }

    @Test
    @DisplayName("a plan the catalog no longer has renders as nulls, not as an error")
    void rendersAVanishedPlan() throws Exception {
        when(catalog.findPlan(any())).thenReturn(Optional.empty());
        when(catalog.findPrice(any())).thenReturn(Optional.empty());

        SubscriptionView view = mappers.toView(onPlan());

        assertThat(view.planCode()).isNull();
        assertThat(view.amount()).isNull();
    }

    @Test
    @DisplayName("a pending downgrade is rendered with the plan it moves to and when")
    void rendersThePendingChange() throws Exception {
        PlanEntity basic = PlanEntity.create("BASIC", "Basic", null, 10);
        PlanPriceEntity cheaper = PlanPriceEntity.create(basic.getId(), new CurrencyCode("USD"), 1000L,
                BillingInterval.MONTH, 0);
        when(catalog.findPrice(cheaper.getId())).thenReturn(Optional.of(cheaper));
        when(catalog.findPlan(basic.getId())).thenReturn(Optional.of(basic));
        StoreSubscriptionEntity entity = onPlan();
        entity.scheduleDowngradeTo(cheaper.getId(), PERIOD_END);

        SubscriptionView view = mappers.toView(entity);

        assertThat(view.pendingPlanChange()).isNotNull();
        assertThat(view.pendingPlanChange().planCode()).isEqualTo("BASIC");
        assertThat(view.pendingPlanChange().effectiveAt()).isEqualTo(PERIOD_END);
        // The plan in force is unchanged — the downgrade has not happened.
        assertThat(view.planCode()).isEqualTo("PRO");
    }

    @Test
    @DisplayName("a pending change whose price the catalog lost still renders, without a plan code")
    void rendersAPendingChangeWithNoPlan() throws Exception {
        PlanPriceId gone = PlanPriceId.newId();
        when(catalog.findPrice(gone)).thenReturn(Optional.empty());
        StoreSubscriptionEntity entity = onPlan();
        entity.scheduleDowngradeTo(gone, PERIOD_END);

        SubscriptionView view = mappers.toView(entity);

        assertThat(view.pendingPlanChange().planCode()).isNull();
        assertThat(view.pendingPlanChange().planPriceId()).isEqualTo(gone);
    }

    @Test
    @DisplayName("nothing pending renders as no pending change at all")
    void rendersNoPendingChange() throws Exception {
        assertThat(mappers.toView(onPlan()).pendingPlanChange()).isNull();
    }

    @Test
    @DisplayName("the entitlement snapshot carries the operable answer every enforcement layer asks for")
    void rendersASnapshot() throws Exception {
        EntitlementSnapshot snapshot = mappers.toSnapshot(onPlan());

        assertThat(snapshot.store()).isEqualTo(STORE);
        assertThat(snapshot.status()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(snapshot.operable()).isTrue();
        assertThat(snapshot.planCode()).isEqualTo("PRO");
        assertThat(snapshot.currentPeriodEnd()).isEqualTo(PERIOD_END);
        assertThat(snapshot.entitlement(EntitlementKey.MAX_PRODUCTS).limitValue()).isEqualTo(500);
        // A key the plan does not mention is unlimited, not forbidden.
        assertThat(snapshot.entitlement(EntitlementKey.MAX_ACCOUNTS).unlimited()).isTrue();
    }

    @Test
    @DisplayName("an unpaid store's snapshot says it may not be worked in")
    void snapshotOfAnUnpaidStore() {
        EntitlementSnapshot snapshot = mappers.toSnapshot(StoreSubscriptionEntity.pending(STORE, ORG));

        assertThat(snapshot.operable()).isFalse();
        assertThat(snapshot.planCode()).isNull();
    }

}
