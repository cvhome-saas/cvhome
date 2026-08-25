package com.asrevo.cvhome.billing.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.billing.commons.BillingInterval;
import com.asrevo.cvhome.billing.commons.EntitlementKey;
import com.asrevo.cvhome.billing.commons.EntitlementValue;
import com.asrevo.cvhome.billing.commons.PlanId;
import com.asrevo.cvhome.billing.commons.PlanPriceId;
import com.asrevo.cvhome.billing.commons.StripePriceId;
import com.asrevo.cvhome.billing.commons.dto.PlanView;
import com.asrevo.cvhome.billing.commons.errors.PlanNotFoundException;
import com.asrevo.cvhome.billing.commons.errors.PlanPriceNotFoundException;
import com.asrevo.cvhome.billing.domain.PlanEntitlementEntity;
import com.asrevo.cvhome.billing.domain.PlanEntity;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;
import com.asrevo.cvhome.billing.mappers.PlanCatalogMappers;
import com.asrevo.cvhome.billing.repository.PlanEntitlementRepository;
import com.asrevo.cvhome.billing.repository.PlanPriceRepository;
import com.asrevo.cvhome.billing.repository.PlanRepository;
import com.asrevo.cvhome.commons.domain.CurrencyCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Reading the plan catalog.
 *
 * <p>
 * The real mapper is used rather than a mock: this service's whole job is assembling a plan out of three aggregates,
 * and a stubbed mapper would let a listing that dropped its prices or its entitlements pass.
 * </p>
 */
class PlanCatalogServiceImplTest {

    private PlanRepository plans;

    private PlanPriceRepository prices;

    private PlanEntitlementRepository entitlements;

    private PlanCatalogServiceImpl service;

    private PlanEntity basic;

    private PlanEntity pro;

    @BeforeEach
    void setUp() {
        plans = mock(PlanRepository.class);
        prices = mock(PlanPriceRepository.class);
        entitlements = mock(PlanEntitlementRepository.class);
        service = new PlanCatalogServiceImpl(plans, prices, entitlements, new PlanCatalogMappers());

        basic = PlanEntity.create("BASIC", "Basic", "For a store finding its feet.", 10);
        pro = PlanEntity.create("PRO", "Pro", "For a store that is growing.", 20);
        when(entitlements.findAllByPlanId(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());
        when(prices.findAllByPlanIdAndActiveTrue(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());
    }

    private static PlanPriceEntity price(PlanId plan, String currency, long amount, BillingInterval interval) {
        return PlanPriceEntity.create(plan, new CurrencyCode(currency), amount, interval, 0);
    }

    // ------------------------------------------------------------------------------------------------ listing

    @Test
    @DisplayName("only active plans are listed, cheapest tier first, each with its prices and grants")
    void listsActivePlans() {
        when(plans.findAllByActiveTrueOrderByTierAsc()).thenReturn(List.of(basic, pro));
        when(prices.findAllByPlanIdAndActiveTrue(basic.getId()))
                .thenReturn(List.of(price(basic.getId(), "USD", 1000L, BillingInterval.MONTH)));
        PlanEntitlementEntity grant = PlanEntitlementEntity.create(basic.getId(),
                EntitlementValue.limit(EntitlementKey.MAX_PRODUCTS, 500));
        when(entitlements.findAllByPlanId(basic.getId())).thenReturn(List.of(grant));

        List<PlanView> views = service.listActivePlans(null);

        assertThat(views).extracting(PlanView::code).containsExactly("BASIC", "PRO");
        assertThat(views.getFirst().prices()).hasSize(1);
        assertThat(views.getFirst().entitlements())
                .containsEntry(EntitlementKey.MAX_PRODUCTS, EntitlementValue.limit(EntitlementKey.MAX_PRODUCTS, 500));
    }

    @Test
    @DisplayName("a currency filter narrows the prices without dropping the plan")
    void filtersPricesByCurrency() {
        when(plans.findAllByActiveTrueOrderByTierAsc()).thenReturn(List.of(basic));
        when(prices.findAllByPlanIdAndActiveTrue(basic.getId())).thenReturn(List.of(
                price(basic.getId(), "USD", 1000L, BillingInterval.MONTH),
                price(basic.getId(), "EUR", 900L, BillingInterval.MONTH)));

        List<PlanView> views = service.listActivePlans("usd");

        // A plan with no price in the asked-for currency is still a plan; the pricing page decides what to do with
        // an empty price list.
        assertThat(views).hasSize(1);
        assertThat(views.getFirst().prices()).hasSize(1);
    }

    @Test
    @DisplayName("prices come back in a fixed order, so a pricing page need not decide one")
    void pricesAreOrdered() {
        when(plans.findAllByActiveTrueOrderByTierAsc()).thenReturn(List.of(basic));
        when(prices.findAllByPlanIdAndActiveTrue(basic.getId())).thenReturn(List.of(
                price(basic.getId(), "USD", 10000L, BillingInterval.YEAR),
                price(basic.getId(), "USD", 1000L, BillingInterval.MONTH)));

        List<PlanView> views = service.listActivePlans(null);

        assertThat(views.getFirst().prices()).extracting(it -> it.interval().name())
                .containsExactly("MONTH", "YEAR");
    }

    // ------------------------------------------------------------------------------------------------ lookups

    @Test
    @DisplayName("a plan is found by code only while it is on sale")
    void requirePlanByCodeSkipsWithdrawnPlans() throws Exception {
        when(plans.findByCode("BASIC")).thenReturn(Optional.of(basic));

        assertThat(service.requirePlanByCode("BASIC")).isSameAs(basic);

        when(plans.findByCode("OLD")).thenReturn(Optional.of(PlanEntity
                .create("OLD", "Old", null, 5).deactivate()));
        // A withdrawn plan is not deleted — subscribers and invoices still point at it — but it cannot be bought.
        assertThatThrownBy(() -> service.requirePlanByCode("OLD")).isInstanceOf(PlanNotFoundException.class);
    }

    @Test
    @DisplayName("a plan absent by code is reported")
    void requirePlanByCodeMissing() {
        when(plans.findByCode("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.requirePlanByCode("NOPE")).isInstanceOf(PlanNotFoundException.class);
    }

    @Test
    @DisplayName("a plan is found by id whether or not it is still on sale")
    void requirePlanByIdIgnoresActive() throws Exception {
        PlanEntity withdrawn = PlanEntity.create("OLD", "Old", null, 5).deactivate();
        when(plans.findById(withdrawn.getId())).thenReturn(Optional.of(withdrawn));

        // Rendering an existing subscriber's plan has to work after the plan is withdrawn.
        assertThat(service.requirePlan(withdrawn.getId())).isSameAs(withdrawn);
    }

    @Test
    @DisplayName("a plan absent by id is reported")
    void requirePlanByIdMissing() {
        PlanId id = PlanId.newId();
        when(plans.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.requirePlan(id)).isInstanceOf(PlanNotFoundException.class);
    }

    @Test
    @DisplayName("a withdrawn price cannot be bought, but can still be read")
    void purchasableVersusReadable() throws Exception {
        PlanPriceEntity withdrawn = price(basic.getId(), "USD", 1000L, BillingInterval.MONTH).deactivate();
        when(prices.findById(withdrawn.getId())).thenReturn(Optional.of(withdrawn));

        assertThatThrownBy(() -> service.requirePurchasablePrice(withdrawn.getId()))
                .isInstanceOf(PlanPriceNotFoundException.class);
        // The subscribers still on it keep their terms, so the row has to keep rendering.
        assertThat(service.requirePrice(withdrawn.getId())).isSameAs(withdrawn);
    }

    @Test
    @DisplayName("a price that does not exist is reported either way")
    void aMissingPriceIsReported() {
        PlanPriceId id = PlanPriceId.newId();
        when(prices.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.requirePurchasablePrice(id)).isInstanceOf(PlanPriceNotFoundException.class);
        assertThatThrownBy(() -> service.requirePrice(id)).isInstanceOf(PlanPriceNotFoundException.class);
    }

    @Test
    @DisplayName("a null Stripe price id looks nothing up")
    void aNullStripePriceIdIsEmpty() {
        assertThat(service.findByStripePriceId(null)).isEmpty();
        org.mockito.Mockito.verifyNoInteractions(prices);
    }

    @Test
    @DisplayName("a Stripe price id is resolved to the local row")
    void findsByStripePriceId() {
        PlanPriceEntity published = price(basic.getId(), "USD", 1000L, BillingInterval.MONTH)
                .publishedAs(new StripePriceId("price_basic"));
        when(prices.findByStripePriceId(new StripePriceId("price_basic"))).thenReturn(Optional.of(published));

        assertThat(service.findByStripePriceId("price_basic")).contains(published);
    }

    @Test
    @DisplayName("findPlan and findPrice answer empty rather than throwing")
    void optionalLookups() {
        PlanId planId = PlanId.newId();
        PlanPriceId priceId = PlanPriceId.newId();
        when(plans.findById(planId)).thenReturn(Optional.empty());
        when(prices.findById(priceId)).thenReturn(Optional.empty());

        assertThat(service.findPlan(planId)).isEmpty();
        assertThat(service.findPrice(priceId)).isEmpty();
    }

    // ------------------------------------------------------------------------------------------ entitlements

    @Test
    @DisplayName("a plan's grants come back keyed by entitlement")
    void entitlementsOfAPlan() {
        when(entitlements.findAllByPlanId(basic.getId())).thenReturn(List.of(
                PlanEntitlementEntity.create(basic.getId(), EntitlementValue.limit(EntitlementKey.MAX_PRODUCTS, 500)),
                PlanEntitlementEntity.create(basic.getId(),
                        EntitlementValue.flag(EntitlementKey.CUSTOM_DOMAIN, true))));

        Map<EntitlementKey, EntitlementValue> map = service.entitlementsOf(basic.getId());

        assertThat(map).hasSize(2);
        assertThat(map.get(EntitlementKey.MAX_PRODUCTS).limitValue()).isEqualTo(500);
        assertThat(map.get(EntitlementKey.CUSTOM_DOMAIN).granted()).isTrue();
    }

    // -------------------------------------------------------------------------------------- cheapest for trial

    @Test
    @DisplayName("the cheapest active price across every plan is what a trial is granted on")
    void cheapestActivePrice() {
        when(plans.findAllByActiveTrueOrderByTierAsc()).thenReturn(List.of(basic, pro));
        when(prices.findAllByPlanIdAndActiveTrue(basic.getId())).thenReturn(List.of(
                price(basic.getId(), "USD", 1000L, BillingInterval.MONTH),
                price(basic.getId(), "USD", 10000L, BillingInterval.YEAR)));
        when(prices.findAllByPlanIdAndActiveTrue(pro.getId())).thenReturn(List.of(
                price(pro.getId(), "USD", 3000L, BillingInterval.MONTH)));

        assertThat(service.cheapestActivePrice()).hasValueSatisfying(it ->
                assertThat(it.getUnitAmount()).isEqualTo(1000L));
    }

    @Test
    @DisplayName("an empty catalog has no cheapest price, and says so rather than failing")
    void cheapestOfAnEmptyCatalog() {
        when(plans.findAllByActiveTrueOrderByTierAsc()).thenReturn(List.of());

        // The caller turns this into PlanNotFoundException with a message an operator can act on; failing here
        // would surface as a stack trace during store provisioning.
        assertThat(service.cheapestActivePrice()).isEmpty();
    }

    @Test
    @DisplayName("prices at the same amount are broken by interval, so the answer is deterministic")
    void cheapestIsDeterministic() {
        when(plans.findAllByActiveTrueOrderByTierAsc()).thenReturn(List.of(basic));
        when(prices.findAllByPlanIdAndActiveTrue(basic.getId())).thenReturn(List.of(
                price(basic.getId(), "USD", 0L, BillingInterval.YEAR),
                price(basic.getId(), "USD", 0L, BillingInterval.MONTH)));

        // A free plan with both intervals is exactly the FREE plan in plan-catalog.yml's shape; without the
        // tiebreak, which store got which interval would depend on row order.
        assertThat(service.cheapestActivePrice()).hasValueSatisfying(it ->
                assertThat(it.getBillingInterval()).isEqualTo(BillingInterval.MONTH));
    }

}
