package com.asrevo.cvhome.billing.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.asrevo.cvhome.billing.commons.BillingInterval;
import com.asrevo.cvhome.billing.commons.EntitlementKey;
import com.asrevo.cvhome.billing.commons.EntitlementValue;
import com.asrevo.cvhome.billing.config.PlanCatalogProperties;
import com.asrevo.cvhome.billing.domain.PlanEntitlementEntity;
import com.asrevo.cvhome.billing.domain.PlanEntity;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;
import com.asrevo.cvhome.billing.repository.PlanEntitlementRepository;
import com.asrevo.cvhome.billing.repository.PlanPriceRepository;
import com.asrevo.cvhome.billing.repository.PlanRepository;
import com.asrevo.cvhome.commons.domain.CurrencyCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Reconciling the database against {@code plan-catalog.yml} on every boot.
 *
 * <p>
 * The seeder has to converge rather than accumulate, and two of its behaviours look like bugs until you know why
 * they are there: a price whose amount changed is <em>not</em> edited — the old row is deactivated and a new one
 * minted, because Stripe prices are immutable and existing subscribers are owed the terms they agreed to — and a plan
 * that disappears from the file is deactivated rather than deleted, because subscriptions and invoices still point
 * at it. Both are pinned here.
 * </p>
 */
class PlanCatalogSeederTest {

    private PlanRepository plans;

    private PlanPriceRepository prices;

    private PlanEntitlementRepository entitlements;

    @BeforeEach
    void setUp() {
        plans = mock(PlanRepository.class);
        prices = mock(PlanPriceRepository.class);
        entitlements = mock(PlanEntitlementRepository.class);
        when(plans.save(any(PlanEntity.class))).thenAnswer(it -> it.getArgument(0, PlanEntity.class));
        when(prices.save(any(PlanPriceEntity.class))).thenAnswer(it -> it.getArgument(0, PlanPriceEntity.class));
        when(plans.findAllByActiveTrueOrderByTierAsc()).thenReturn(List.of());
        when(prices.findAllByPlanId(any())).thenReturn(List.of());
        when(entitlements.findAllByPlanId(any())).thenReturn(List.of());
    }

    private PlanCatalogSeeder seederFor(PlanCatalogProperties.Plan... declared) {
        return new PlanCatalogSeeder(new PlanCatalogProperties(true, false, List.of(declared)), plans, prices,
                entitlements);
    }

    private static PlanCatalogProperties.Plan plan(String code, long amount,
                                                   Map<EntitlementKey, String> grants) {
        return new PlanCatalogProperties.Plan(code, code.charAt(0) + code.substring(1).toLowerCase(),
                "A plan.", 10,
                List.of(new PlanCatalogProperties.Price("USD", amount, BillingInterval.MONTH, 0)), grants);
    }

    private List<PlanPriceEntity> savedPrices() {
        ArgumentCaptor<PlanPriceEntity> captor = ArgumentCaptor.forClass(PlanPriceEntity.class);
        verify(prices, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        return captor.getAllValues();
    }

    // ---------------------------------------------------------------------------------------------- creating

    @Test
    @DisplayName("a plan the database has never seen is created with its price and grants")
    void createsANewPlan() {
        when(plans.findByCode("BASIC")).thenReturn(Optional.empty());

        seederFor(plan("BASIC", 1000L, Map.of(EntitlementKey.MAX_PRODUCTS, "500"))).run(null);

        ArgumentCaptor<PlanEntity> saved = ArgumentCaptor.forClass(PlanEntity.class);
        verify(plans).save(saved.capture());
        assertThat(saved.getValue().getCode()).isEqualTo("BASIC");
        assertThat(saved.getValue().isActive()).isTrue();
        assertThat(savedPrices()).singleElement().satisfies(price -> {
            assertThat(price.getUnitAmount()).isEqualTo(1000L);
            assertThat(price.getCurrency()).isEqualTo(new CurrencyCode("USD"));
        });
        ArgumentCaptor<PlanEntitlementEntity> grant = ArgumentCaptor.forClass(PlanEntitlementEntity.class);
        verify(entitlements).save(grant.capture());
        assertThat(grant.getValue().getLimitValue()).isEqualTo(500);
    }

    @Test
    @DisplayName("a plan already there is re-described rather than duplicated, and its code is never touched")
    void updatesAnExistingPlan() {
        PlanEntity existing = PlanEntity.create("BASIC", "Old name", "Old blurb", 5);
        when(plans.findByCode("BASIC")).thenReturn(Optional.of(existing));
        when(prices.findAllByPlanId(existing.getId())).thenReturn(List.of(
                PlanPriceEntity.create(existing.getId(), new CurrencyCode("USD"), 1000L, BillingInterval.MONTH, 0)));

        seederFor(plan("BASIC", 1000L, Map.of())).run(null);

        ArgumentCaptor<PlanEntity> saved = ArgumentCaptor.forClass(PlanEntity.class);
        verify(plans).save(saved.capture());
        assertThat(saved.getValue()).isSameAs(existing);
        assertThat(saved.getValue().getDisplayName()).isEqualTo("Basic");
        assertThat(saved.getValue().getTier()).isEqualTo(10);
        // The code is the plan's identity: renaming one would create a second and orphan the subscribers of the
        // first, so `describe` cannot change it.
        assertThat(saved.getValue().getCode()).isEqualTo("BASIC");
    }

    @Test
    @DisplayName("an unchanged price is left completely alone, so a restart writes nothing")
    void convergesRatherThanAccumulates() {
        PlanEntity existing = PlanEntity.create("BASIC", "Basic", "A plan.", 10);
        when(plans.findByCode("BASIC")).thenReturn(Optional.of(existing));
        when(prices.findAllByPlanId(existing.getId())).thenReturn(List.of(
                PlanPriceEntity.create(existing.getId(), new CurrencyCode("USD"), 1000L, BillingInterval.MONTH, 0)));

        seederFor(plan("BASIC", 1000L, Map.of())).run(null);

        // This is what makes the seeder safe to run on every boot.
        verify(prices, never()).save(any(PlanPriceEntity.class));
    }

    // ------------------------------------------------------------------------------------------ price changes

    @Test
    @DisplayName("a changed amount mints a new price and withdraws the old one instead of editing it")
    void aChangedAmountMintsANewPrice() {
        PlanEntity existing = PlanEntity.create("BASIC", "Basic", "A plan.", 10);
        PlanPriceEntity oldPrice = PlanPriceEntity.create(existing.getId(), new CurrencyCode("USD"), 1000L,
                BillingInterval.MONTH, 0);
        when(plans.findByCode("BASIC")).thenReturn(Optional.of(existing));
        when(prices.findAllByPlanId(existing.getId())).thenReturn(List.of(oldPrice));

        seederFor(plan("BASIC", 1500L, Map.of())).run(null);

        List<PlanPriceEntity> saved = savedPrices();
        assertThat(saved).hasSize(2);
        // Stripe prices are immutable, and the subscribers on the old one are owed the terms they agreed to.
        assertThat(saved.getFirst()).isSameAs(oldPrice);
        assertThat(saved.getFirst().isActive()).isFalse();
        assertThat(saved.getLast().getUnitAmount()).isEqualTo(1500L);
        assertThat(saved.getLast().isActive()).isTrue();
    }

    @Test
    @DisplayName("a price dropped from the file is withdrawn, not deleted")
    void anUndeclaredPriceIsWithdrawn() {
        PlanEntity existing = PlanEntity.create("BASIC", "Basic", "A plan.", 10);
        PlanPriceEntity monthly = PlanPriceEntity.create(existing.getId(), new CurrencyCode("USD"), 1000L,
                BillingInterval.MONTH, 0);
        PlanPriceEntity yearly = PlanPriceEntity.create(existing.getId(), new CurrencyCode("USD"), 10000L,
                BillingInterval.YEAR, 0);
        when(plans.findByCode("BASIC")).thenReturn(Optional.of(existing));
        when(prices.findAllByPlanId(existing.getId())).thenReturn(List.of(monthly, yearly));

        seederFor(plan("BASIC", 1000L, Map.of())).run(null);

        assertThat(savedPrices()).singleElement().satisfies(price -> {
            assertThat(price).isSameAs(yearly);
            assertThat(price.isActive()).isFalse();
        });
    }

    @Test
    @DisplayName("an already-withdrawn price is not withdrawn a second time")
    void anInactivePriceIsSkipped() {
        PlanEntity existing = PlanEntity.create("BASIC", "Basic", "A plan.", 10);
        PlanPriceEntity retired = PlanPriceEntity.create(existing.getId(), new CurrencyCode("USD"), 800L,
                BillingInterval.MONTH, 0).deactivate();
        PlanPriceEntity current = PlanPriceEntity.create(existing.getId(), new CurrencyCode("USD"), 1000L,
                BillingInterval.MONTH, 0);
        when(plans.findByCode("BASIC")).thenReturn(Optional.of(existing));
        when(prices.findAllByPlanId(existing.getId())).thenReturn(List.of(retired, current));

        seederFor(plan("BASIC", 1000L, Map.of())).run(null);

        // The retired row is matched neither as the current price nor as an undeclared one; a build that touched it
        // would rewrite history on every boot.
        verify(prices, never()).save(any(PlanPriceEntity.class));
    }

    // ------------------------------------------------------------------------------------------ entitlements

    @Test
    @DisplayName("a numeric grant is parsed, and an unparseable one becomes unlimited rather than failing the boot")
    void parsesNumericGrants() {
        when(plans.findByCode("BASIC")).thenReturn(Optional.empty());

        seederFor(plan("BASIC", 1000L, Map.of(EntitlementKey.MAX_PRODUCTS, "not-a-number"))).run(null);

        ArgumentCaptor<PlanEntitlementEntity> grant = ArgumentCaptor.forClass(PlanEntitlementEntity.class);
        verify(entitlements).save(grant.capture());
        // A typo in one line of the catalog should not take billing offline.
        assertThat(grant.getValue().getLimitValue()).isNull();
        assertThat(grant.getValue().getFlagValue()).isNull();
    }

    @Test
    @DisplayName("a blank value means unlimited, which is not the same as zero")
    void aBlankValueIsUnlimited() {
        when(plans.findByCode("BASIC")).thenReturn(Optional.empty());

        seederFor(plan("BASIC", 1000L, Map.of(EntitlementKey.MAX_PRODUCTS, "   "))).run(null);

        ArgumentCaptor<PlanEntitlementEntity> grant = ArgumentCaptor.forClass(PlanEntitlementEntity.class);
        verify(entitlements).save(grant.capture());
        assertThat(grant.getValue().value().unlimited()).isTrue();
    }

    @Test
    @DisplayName("a capability grant is parsed as a flag")
    void parsesFlagGrants() {
        when(plans.findByCode("BASIC")).thenReturn(Optional.empty());

        seederFor(plan("BASIC", 1000L, Map.of(EntitlementKey.CUSTOM_DOMAIN, "true"))).run(null);

        ArgumentCaptor<PlanEntitlementEntity> grant = ArgumentCaptor.forClass(PlanEntitlementEntity.class);
        verify(entitlements).save(grant.capture());
        assertThat(grant.getValue().getFlagValue()).isTrue();
        assertThat(grant.getValue().getLimitValue()).isNull();
    }

    @Test
    @DisplayName("a grant already on the plan is updated in place")
    void updatesAnExistingGrant() {
        PlanEntity existing = PlanEntity.create("BASIC", "Basic", "A plan.", 10);
        PlanEntitlementEntity grant = PlanEntitlementEntity.create(existing.getId(),
                EntitlementValue.limit(EntitlementKey.MAX_PRODUCTS, 100));
        when(plans.findByCode("BASIC")).thenReturn(Optional.of(existing));
        when(entitlements.findAllByPlanId(existing.getId())).thenReturn(List.of(grant));

        seederFor(plan("BASIC", 1000L, Map.of(EntitlementKey.MAX_PRODUCTS, "500"))).run(null);

        ArgumentCaptor<PlanEntitlementEntity> saved = ArgumentCaptor.forClass(PlanEntitlementEntity.class);
        verify(entitlements).save(saved.capture());
        assertThat(saved.getValue()).isSameAs(grant);
        assertThat(saved.getValue().getLimitValue()).isEqualTo(500);
    }

    @Test
    @DisplayName("a key dropped from the file is deleted, so its absence means unlimited again")
    void aDroppedGrantIsDeleted() {
        PlanEntity existing = PlanEntity.create("BASIC", "Basic", "A plan.", 10);
        PlanEntitlementEntity stale = PlanEntitlementEntity.create(existing.getId(),
                EntitlementValue.limit(EntitlementKey.MAX_ORDERS_MONTH, 50));
        when(plans.findByCode("BASIC")).thenReturn(Optional.of(existing));
        when(entitlements.findAllByPlanId(existing.getId())).thenReturn(List.of(stale));

        seederFor(plan("BASIC", 1000L, Map.of())).run(null);

        // Unlike a plan or a price, a grant row has nothing pointing at it, and its absence already means
        // "unlimited" everywhere else.
        verify(entitlements).delete(stale);
    }

    // ------------------------------------------------------------------------------------------------ plans

    @Test
    @DisplayName("a plan no longer declared is withdrawn from sale, not deleted")
    void anUndeclaredPlanIsWithdrawn() {
        PlanEntity gone = PlanEntity.create("LEGACY", "Legacy", null, 1);
        when(plans.findByCode("BASIC")).thenReturn(Optional.empty());
        when(plans.findAllByActiveTrueOrderByTierAsc()).thenReturn(List.of(gone));

        seederFor(plan("BASIC", 1000L, Map.of())).run(null);

        ArgumentCaptor<PlanEntity> saved = ArgumentCaptor.forClass(PlanEntity.class);
        verify(plans, org.mockito.Mockito.atLeastOnce()).save(saved.capture());
        // Subscriptions and invoices still point at it, and their pages have to keep rendering.
        assertThat(saved.getAllValues()).anySatisfy(plan -> {
            assertThat(plan.getCode()).isEqualTo("LEGACY");
            assertThat(plan.isActive()).isFalse();
        });
    }

    @Test
    @DisplayName("a still-declared plan is not withdrawn by the sweep that follows")
    void aDeclaredPlanSurvivesTheSweep() {
        PlanEntity existing = PlanEntity.create("BASIC", "Basic", "A plan.", 10);
        when(plans.findByCode("BASIC")).thenReturn(Optional.of(existing));
        when(plans.findAllByActiveTrueOrderByTierAsc()).thenReturn(List.of(existing));

        seederFor(plan("BASIC", 1000L, Map.of())).run(null);

        assertThat(existing.isActive()).isTrue();
    }

    @Test
    @DisplayName("an empty catalog leaves the database untouched rather than withdrawing everything")
    void anEmptyCatalogIsARefusal() {
        seederFor().run(null);

        // Seeding switched on with nothing declared is a misconfiguration, and acting on it would take every plan
        // on the platform off sale on one bad deploy.
        verify(plans, never()).save(any(PlanEntity.class));
        verify(plans, never()).findAllByActiveTrueOrderByTierAsc();
    }

}
