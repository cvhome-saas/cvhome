package com.asrevo.cvhome.billing.service;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.billing.commons.BillingInterval;
import com.asrevo.cvhome.billing.commons.StripePriceId;
import com.asrevo.cvhome.billing.commons.StripeProductId;
import com.asrevo.cvhome.billing.commons.StripeRequestOperation;
import com.asrevo.cvhome.billing.commons.errors.BillingProviderUnavailableException;
import com.asrevo.cvhome.billing.domain.PlanEntity;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;
import com.asrevo.cvhome.billing.repository.PlanPriceRepository;
import com.asrevo.cvhome.billing.repository.PlanRepository;
import com.asrevo.cvhome.billing.service.stripe.StripeCatalogGateway;
import com.asrevo.cvhome.commons.domain.CurrencyCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pushing catalog rows Stripe has never seen, so they become purchasable.
 *
 * <p>
 * Converges rather than republishing: only rows whose Stripe id is still null are pushed. And a failure is logged
 * rather than thrown, because everything that does not involve taking money — the catalog, existing subscriptions,
 * every entitlement read — works without Stripe, and refusing to start would take all of that down whenever Stripe
 * was unreachable.
 * </p>
 */
class PlanCatalogPublisherTest {

    private PlanRepository plans;

    private PlanPriceRepository prices;

    private StripeCatalogGateway gateway;

    private PlanCatalogPublisher publisher;

    @BeforeEach
    void setUp() {
        plans = mock(PlanRepository.class);
        prices = mock(PlanPriceRepository.class);
        gateway = mock(StripeCatalogGateway.class);
        publisher = new PlanCatalogPublisher(plans, prices, gateway);
        when(plans.save(any(PlanEntity.class))).thenAnswer(it -> it.getArgument(0, PlanEntity.class));
        when(prices.save(any(PlanPriceEntity.class))).thenAnswer(it -> it.getArgument(0, PlanPriceEntity.class));
    }

    private static PlanPriceEntity price(PlanEntity plan) {
        return PlanPriceEntity.create(plan.getId(), new CurrencyCode("USD"), 1000L, BillingInterval.MONTH, 0);
    }

    @Test
    @DisplayName("an unpublished plan and its prices are pushed and their ids written back")
    void publishesWhatHasNoStripeId() throws Exception {
        PlanEntity plan = PlanEntity.create("BASIC", "Basic", "A plan.", 10);
        PlanPriceEntity monthly = price(plan);
        when(plans.findAllByActiveTrueOrderByTierAsc()).thenReturn(List.of(plan));
        when(prices.findAllByPlanIdAndActiveTrue(plan.getId())).thenReturn(List.of(monthly));
        when(gateway.createProduct(any())).thenReturn(new StripeProductId("prod_1"));
        when(gateway.createPrice(any(), any())).thenReturn(new StripePriceId("price_1"));

        publisher.publish();

        assertThat(plan.getStripeProductId()).isEqualTo(new StripeProductId("prod_1"));
        assertThat(monthly.getStripePriceId()).isEqualTo(new StripePriceId("price_1"));
    }

    @Test
    @DisplayName("a plan already published is not published again")
    void skipsWhatIsAlreadyPublished() throws Exception {
        PlanEntity plan = PlanEntity.create("BASIC", "Basic", "A plan.", 10)
                .publishedAs(new StripeProductId("prod_1"));
        PlanPriceEntity monthly = price(plan).publishedAs(new StripePriceId("price_1"));
        when(plans.findAllByActiveTrueOrderByTierAsc()).thenReturn(List.of(plan));
        when(prices.findAllByPlanIdAndActiveTrue(plan.getId())).thenReturn(List.of(monthly));

        publisher.publish();

        // Runs on every boot; republishing would mint a duplicate product and price per restart.
        verify(gateway, never()).createProduct(any());
        verify(gateway, never()).createPrice(any(), any());
        verify(plans, never()).save(any(PlanEntity.class));
    }

    @Test
    @DisplayName("a price added to an already published plan is published on its own")
    void publishesANewPriceOfAPublishedPlan() throws Exception {
        PlanEntity plan = PlanEntity.create("BASIC", "Basic", "A plan.", 10)
                .publishedAs(new StripeProductId("prod_1"));
        PlanPriceEntity fresh = price(plan);
        when(plans.findAllByActiveTrueOrderByTierAsc()).thenReturn(List.of(plan));
        when(prices.findAllByPlanIdAndActiveTrue(plan.getId())).thenReturn(List.of(fresh));
        when(gateway.createPrice(any(), any())).thenReturn(new StripePriceId("price_2"));

        publisher.publish();

        verify(gateway, never()).createProduct(any());
        assertThat(fresh.getStripePriceId()).isEqualTo(new StripePriceId("price_2"));
    }

    @Test
    @DisplayName("Stripe being unreachable does not stop the service starting")
    void aFailureDoesNotAbortTheBoot() throws Exception {
        PlanEntity plan = PlanEntity.create("BASIC", "Basic", "A plan.", 10);
        when(plans.findAllByActiveTrueOrderByTierAsc()).thenReturn(List.of(plan));
        when(gateway.createProduct(any())).thenThrow(BillingProviderUnavailableException.of("stripe", null,
                StripeRequestOperation.PRODUCT_CREATE, null, 0, null));

        // The plan stays unpurchasable until the next start, and checkout reports that as "not purchasable" rather
        // than as a provider fault. Everything else keeps working.
        assertThatCode(publisher::publish).doesNotThrowAnyException();
        assertThat(plan.getStripeProductId()).isNull();
    }

    @Test
    @DisplayName("a failure stops the run rather than hammering Stripe for every remaining plan")
    void aFailureStopsTheRun() throws Exception {
        PlanEntity first = PlanEntity.create("BASIC", "Basic", "A plan.", 10);
        PlanEntity second = PlanEntity.create("PRO", "Pro", "A plan.", 20);
        when(plans.findAllByActiveTrueOrderByTierAsc()).thenReturn(List.of(first, second));
        when(gateway.createProduct(any())).thenThrow(BillingProviderUnavailableException.of("stripe", null,
                StripeRequestOperation.PRODUCT_CREATE, null, 0, null));

        publisher.publish();

        // Whatever made the first call fail will make the rest fail too; a full sweep would only be a slower boot
        // and a longer log.
        verify(gateway, org.mockito.Mockito.times(1)).createProduct(any());
    }

}
