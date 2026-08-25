package com.asrevo.cvhome.billing.service.stripe;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.asrevo.cvhome.billing.commons.BillingInterval;
import com.asrevo.cvhome.billing.commons.StripePriceId;
import com.asrevo.cvhome.billing.commons.StripeProductId;
import com.asrevo.cvhome.billing.commons.StripeRequestOperation;
import com.asrevo.cvhome.billing.commons.errors.BillingProviderUnavailableException;
import com.asrevo.cvhome.billing.config.StripeCredentials;
import com.asrevo.cvhome.billing.domain.PlanEntity;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;
import com.asrevo.cvhome.billing.domain.StripeRequestEntity;
import com.asrevo.cvhome.billing.repository.StripeRequestRepository;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.stripe.StripeClient;
import com.stripe.exception.ApiConnectionException;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.net.RequestOptions;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.ProductCreateParams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Publishing the local plan catalog to Stripe.
 *
 * <p>
 * The idempotency keys are the substance here. Both are derived purely from the catalog row with no time component,
 * because publishing the same plan twice must produce the same product however far apart the attempts are — that is
 * exactly what makes the sync safe to run on every boot, and a key that quietly grew a timestamp would mint a
 * duplicate product per restart without any test noticing.
 * </p>
 */
class StripeCatalogGatewayTest {

    private static final String API_KEY = "sk_test_catalog";

    private StripeClient stripe;

    private StripeRequestRepository requests;

    private StripeCatalogGateway gateway;

    @BeforeEach
    void setUp() {
        StripeCredentials credentials = mock(StripeCredentials.class);
        when(credentials.apiKey()).thenReturn(API_KEY);
        stripe = mock(StripeClient.class, RETURNS_DEEP_STUBS);
        requests = mock(StripeRequestRepository.class);
        when(requests.existsById(anyString())).thenReturn(false);
        when(requests.findById(anyString())).thenReturn(java.util.Optional.empty());
        gateway = new StripeCatalogGateway(credentials, requests, stripe);
    }

    private static PlanEntity plan() {
        PlanEntity plan = PlanEntity.create("PRO", "Pro", "For a store that is growing.", 20);
        return plan.publishedAs(new StripeProductId("prod_pro"));
    }

    private static PlanPriceEntity price() {
        return PlanPriceEntity.create(plan().getId(), new CurrencyCode("USD"), 3000L, BillingInterval.MONTH, 0);
    }

    @Test
    @DisplayName("a plan is published as a product and its id comes back")
    void createsAProduct() throws Exception {
        Product created = mock(Product.class);
        when(created.getId()).thenReturn("prod_new");
        when(stripe.products().create(any(ProductCreateParams.class), any(RequestOptions.class))).thenReturn(created);

        StripeProductId id = gateway.createProduct(plan());

        assertThat(id).isEqualTo(new StripeProductId("prod_new"));
    }

    @Test
    @DisplayName("the product's idempotency key is the plan code alone, with no time in it")
    void productKeyIsStableForever() throws Exception {
        Product created = mock(Product.class);
        when(created.getId()).thenReturn("prod_new");
        when(stripe.products().create(any(ProductCreateParams.class), any(RequestOptions.class))).thenReturn(created);

        gateway.createProduct(plan());

        ArgumentCaptor<RequestOptions> options = ArgumentCaptor.forClass(RequestOptions.class);
        verify(stripe.products()).create(any(ProductCreateParams.class), options.capture());
        // Exactly this string, restart after restart. A time component here would mint a second Stripe product for
        // the same plan on every boot.
        assertThat(options.getValue().getIdempotencyKey()).isEqualTo("product:PRO");
        assertThat(options.getValue().getApiKey()).isEqualTo(API_KEY);
    }

    @Test
    @DisplayName("the price's key carries everything that makes the price a different price")
    void priceKeyCoversTheWholeShape() throws Exception {
        Price created = mock(Price.class);
        when(created.getId()).thenReturn("price_new");
        when(stripe.prices().create(any(PriceCreateParams.class), any(RequestOptions.class))).thenReturn(created);

        gateway.createPrice(plan(), price());

        ArgumentCaptor<RequestOptions> options = ArgumentCaptor.forClass(RequestOptions.class);
        verify(stripe.prices()).create(any(PriceCreateParams.class), options.capture());
        // Currency, interval and amount are all in it, because Stripe prices are immutable: a changed amount is a
        // new price, and it must not reuse the old one's key.
        assertThat(options.getValue().getIdempotencyKey()).isEqualTo("price:PRO:USD:MONTH:3000");
    }

    @Test
    @DisplayName("the price is created against the plan's published product, in lower-case currency")
    void priceIsBuiltFromTheCatalogRow() throws Exception {
        Price created = mock(Price.class);
        when(created.getId()).thenReturn("price_new");
        when(stripe.prices().create(any(PriceCreateParams.class), any(RequestOptions.class))).thenReturn(created);

        StripePriceId id = gateway.createPrice(plan(), price());

        ArgumentCaptor<PriceCreateParams> params = ArgumentCaptor.forClass(PriceCreateParams.class);
        verify(stripe.prices()).create(params.capture(), any(RequestOptions.class));
        assertThat(id).isEqualTo(new StripePriceId("price_new"));
        assertThat(params.getValue().getProduct()).isEqualTo("prod_pro");
        // Stripe rejects an upper-case currency code.
        assertThat(params.getValue().getCurrency()).isEqualTo("usd");
        assertThat(params.getValue().getUnitAmount()).isEqualTo(3000L);
        assertThat(params.getValue().getRecurring().getInterval())
                .isEqualTo(PriceCreateParams.Recurring.Interval.MONTH);
    }

    @Test
    @DisplayName("a yearly price maps to Stripe's yearly interval")
    void yearlyInterval() throws Exception {
        Price created = mock(Price.class);
        when(created.getId()).thenReturn("price_year");
        when(stripe.prices().create(any(PriceCreateParams.class), any(RequestOptions.class))).thenReturn(created);
        PlanEntity plan = plan();
        PlanPriceEntity yearly = PlanPriceEntity.create(plan.getId(), new CurrencyCode("USD"), 30000L,
                BillingInterval.YEAR, 0);

        gateway.createPrice(plan, yearly);

        ArgumentCaptor<PriceCreateParams> params = ArgumentCaptor.forClass(PriceCreateParams.class);
        verify(stripe.prices()).create(params.capture(), any(RequestOptions.class));
        assertThat(params.getValue().getRecurring().getInterval())
                .isEqualTo(PriceCreateParams.Recurring.Interval.YEAR);
    }

    @Test
    @DisplayName("the intent is recorded before the call and the completion after it")
    void recordsIntentThenCompletion() throws Exception {
        Product created = mock(Product.class);
        when(created.getId()).thenReturn("prod_new");
        when(stripe.products().create(any(ProductCreateParams.class), any(RequestOptions.class))).thenReturn(created);
        StripeRequestEntity intent = StripeRequestEntity.intent("product:PRO", null,
                StripeRequestOperation.PRODUCT_CREATE);
        when(requests.findById("product:PRO")).thenReturn(java.util.Optional.of(intent));

        gateway.createProduct(plan());

        // Two saves: the intent, then the same row completed. A row with no completed_at is what tells a retry the
        // call may already have reached Stripe.
        ArgumentCaptor<StripeRequestEntity> saved = ArgumentCaptor.forClass(StripeRequestEntity.class);
        verify(requests, org.mockito.Mockito.times(2)).save(saved.capture());
        assertThat(saved.getAllValues().getFirst().getOperation())
                .isEqualTo(StripeRequestOperation.PRODUCT_CREATE);
        assertThat(saved.getAllValues().getLast().getStripeObjectId()).isEqualTo("prod_new");
    }

    @Test
    @DisplayName("an intent already on file is not written twice")
    void doesNotDuplicateAnExistingIntent() throws Exception {
        when(requests.existsById("product:PRO")).thenReturn(true);
        Product created = mock(Product.class);
        when(created.getId()).thenReturn("prod_new");
        when(stripe.products().create(any(ProductCreateParams.class), any(RequestOptions.class))).thenReturn(created);

        gateway.createProduct(plan());

        // A retry under the same key finds the row and leaves it alone — overwriting it would lose created_at,
        // which is the only evidence of how long the first attempt has been outstanding.
        verify(requests, never()).save(org.mockito.ArgumentMatchers.argThat(it -> it.getCompletedAt() == null));
    }

    @Test
    @DisplayName("a product Stripe could not be reached for is a provider fault, never a refusal")
    void productFailureIsUnavailable() throws Exception {
        when(stripe.products().create(any(ProductCreateParams.class), any(RequestOptions.class)))
                .thenThrow(new ApiConnectionException("no route to stripe"));

        // No CardException branch on this path on purpose: publishing a catalog moves no money, so there is no
        // refusal to tell apart from a fault.
        assertThatThrownBy(() -> gateway.createProduct(plan()))
                .isInstanceOf(BillingProviderUnavailableException.class);
    }

    @Test
    @DisplayName("a price Stripe could not be reached for is a provider fault too")
    void priceFailureIsUnavailable() throws Exception {
        when(stripe.prices().create(any(PriceCreateParams.class), any(RequestOptions.class)))
                .thenThrow(new ApiConnectionException("no route to stripe"));

        assertThatThrownBy(() -> gateway.createPrice(plan(), price()))
                .isInstanceOf(BillingProviderUnavailableException.class);
    }

}
