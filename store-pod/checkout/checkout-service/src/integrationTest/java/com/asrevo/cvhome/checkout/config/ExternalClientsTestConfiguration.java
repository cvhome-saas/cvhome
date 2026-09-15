package com.asrevo.cvhome.checkout.config;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.asrevo.cvhome.catalog.model.product.ProductDescription;
import com.asrevo.cvhome.catalog.model.product.ReadableCartLineProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableImage;
import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableVariantOptionValue;
import com.asrevo.cvhome.catalog.model.product.ReadableVariantSelection;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.Sku;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.model.AvailabilityQuery;
import com.asrevo.cvhome.inventory.model.SkuInventory;
import com.asrevo.cvhome.inventory.model.SkuPrice;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationCommitResult;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationReleaseResult;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationReserveResult;
import com.asrevo.cvhome.inventory.services.ExternalInventoryService;
import com.asrevo.cvhome.inventory.services.ExternalProductReservationService;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateStatus;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentResponse;
import com.asrevo.cvhome.payment.services.payment.ExternalPaymentGatewayService;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.asrevo.cvhome.testsupport.security.Tokens;

import static org.mockito.ArgumentMatchers.any;

/**
 * The four neighbours checkout talks to, stubbed with a coherent default world: every sku exists at $10 with plenty
 * of stock, except {@link #SKU_OUT} (flagged not purchasable) and {@link #SKU_UNKNOWN} (absent everywhere); store 1
 * allows guest checkout, store 2 requires login; inventory always reserves, commits and releases; payment answers
 * PENDING with a redirect for card types and none for the offline ones.
 *
 * <p>
 * Declared {@code @Primary} so every checkout integration test shares one context and one Postgres. A test that
 * re-stubs a failure calls {@link #reset(ExternalProductReservationService, ExternalPaymentGatewayService)} first
 * and puts the defaults back afterwards, or it silently changes what runs next.
 * </p>
 */
@TestConfiguration(proxyBeanMethods = false)
public class ExternalClientsTestConfiguration {

    public static final String SKU_OUT = "SKU-OUT";

    public static final String SKU_UNKNOWN = "SKU-UNKNOWN";

    public static final String SKU_VARIANT = "SKU-VARIANT-L";

    public static final BigDecimal PRICE = new BigDecimal("10.00");

    public static final String REDIRECT = "https://pay.example/session";

    public static final String GATEWAY_REF = "tx-";

    /**
     * Set when catalog, inventory or merchant is called while the calling thread holds a database transaction: the
     * shape the 2026-09-14 spike collapsed on, three connections held while catalog took seconds. A test resets it and
     * asserts it stayed false.
     */
    public static final AtomicBoolean PRICED_INSIDE_A_TRANSACTION = new AtomicBoolean();

    /** How many times checkout asked the catalogue for cart lines: a read must not, once the line remembers. */
    public static final AtomicInteger CART_LINE_READS = new AtomicInteger();

    /** How many times checkout asked inventory for price and stock: two reads seconds apart cost one. */
    public static final AtomicInteger INVENTORY_READS = new AtomicInteger();

    @Bean
    @Primary
    ExternalMerchantStoreService stubExternalMerchantStoreService() {
        ExternalMerchantStoreService service = Mockito.mock(ExternalMerchantStoreService.class);
        Mockito.when(service.getStore(any())).thenAnswer(invocation -> {
            recordTransaction();
            StoreMerchantId store = invocation.getArgument(0, StoreMerchantId.class);
            ReadableMerchantStore merchantStore = new ReadableMerchantStore();
            merchantStore.setId(store.getId());
            merchantStore.setOrg(Tokens.ORG_1);
            merchantStore.setCurrency(new CurrencyCode("USD"));
            merchantStore.setRequireLoginForOrderPlacement(Tokens.STORE_2.equals(store.getId()));
            return merchantStore;
        });
        return service;
    }

    @Bean
    @Primary
    ExternalProductService stubExternalProductService() {
        ExternalProductService service = Mockito.mock(ExternalProductService.class);
        Mockito.when(service.getDetailedProducts(any(), any(), any())).thenAnswer(invocation -> {
            recordTransaction();
            List<Sku> skus = invocation.getArgument(1);
            return skus.stream().filter(sku -> !SKU_UNKNOWN.equals(sku.value()))
                    .map(ExternalClientsTestConfiguration::product)
                    .toList();
        });
        Mockito.when(service.getCartLines(any(), any(), any())).thenAnswer(invocation -> {
            recordTransaction();
            CART_LINE_READS.incrementAndGet();
            List<Sku> skus = invocation.getArgument(1);
            return skus.stream().filter(sku -> !SKU_UNKNOWN.equals(sku.value()))
                    .map(ExternalClientsTestConfiguration::cartLine)
                    .toList();
        });
        return service;
    }

    @Bean
    @Primary
    ExternalInventoryService stubExternalInventoryService() {
        ExternalInventoryService service = Mockito.mock(ExternalInventoryService.class);
        Mockito.when(service.queryBySkus(any(), any())).thenAnswer(invocation -> {
            recordTransaction();
            INVENTORY_READS.incrementAndGet();
            AvailabilityQuery query = invocation.getArgument(1);
            return query.skus().stream().filter(sku -> !SKU_UNKNOWN.equals(sku.value()))
                    .map(ExternalClientsTestConfiguration::stock).toList();
        });
        return service;
    }

    @Bean
    @Primary
    ExternalProductReservationService stubExternalProductReservationService() throws Exception {
        ExternalProductReservationService service = Mockito.mock(ExternalProductReservationService.class);
        stubReservationDefaults(service);
        return service;
    }

    @Bean
    @Primary
    ExternalPaymentGatewayService stubExternalPaymentGatewayService() throws Exception {
        ExternalPaymentGatewayService service = Mockito.mock(ExternalPaymentGatewayService.class);
        stubPaymentDefaults(service);
        return service;
    }

    private static void recordTransaction() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            PRICED_INSIDE_A_TRANSACTION.set(true);
        }
    }

    public static void stubReservationDefaults(ExternalProductReservationService service) throws Exception {
        Mockito.doAnswer(invocation -> new ProductReservationReserveResult(true, 1L,
                Instant.now().plus(Duration.ofMinutes(45)))).when(service).reserve(any(), any(), any());
        Mockito.doAnswer(invocation -> new ProductReservationCommitResult(true, 1L, null))
                .when(service).commit(any(), any());
        Mockito.doAnswer(invocation -> new ProductReservationReleaseResult(true, 1L, null))
                .when(service).release(any(), any());
    }

    public static void stubPaymentDefaults(ExternalPaymentGatewayService service) throws Exception {
        Mockito.doAnswer(invocation -> {
            PaymentRequest request = invocation.getArgument(1);
            boolean redirect = request.paymentType() == PaymentType.STRIPE || request.paymentType() == PaymentType.PAYPAL;
            return new PaymentInitiateResult(PaymentInitiateStatus.PENDING, redirect ? REDIRECT : null, "ext",
                    GATEWAY_REF + UUID.randomUUID());
        }).when(service).initiatePayment(any(), any());
        Mockito.doAnswer(invocation -> PaymentResponse.builder().status(PaymentStatus.PENDING).build())
                .when(service).status(any(), any());
    }

    /** Forgets every stub, including the defaults; call one of the {@code stub*Defaults} afterwards. */
    public static void reset(ExternalProductReservationService reservations, ExternalPaymentGatewayService payments)
            throws Exception {
        Mockito.reset(reservations, payments);
        stubReservationDefaults(reservations);
        stubPaymentDefaults(payments);
    }

    /** The cart-line shape of {@link #product}: the same product, as a line renders it. */
    private static ReadableCartLineProduct cartLine(Sku sku) {
        ReadableMinimalProduct product = product(sku);
        ReadableCartLineProduct line = new ReadableCartLineProduct();
        line.setSku(sku);
        line.setProductId(product.getId());
        line.setName(product.getDescription().getName());
        line.setFriendlyUrl(product.getDescription().getFriendlyUrl());
        line.setImageUrl(product.getImage().getImageUrl());
        line.setAvailable(product.isAvailable());
        line.setVariant(product.getVariant());
        return line;
    }

    private static ReadableMinimalProduct product(Sku sku) {
        ReadableMinimalProduct product = new ReadableMinimalProduct();
        product.setId((long) Math.abs(sku.value().hashCode() % 10_000));
        product.setSku(sku);
        product.setAvailable(true);
        ProductDescription description = new ProductDescription();
        description.setName(String.format("Product %s", sku));
        product.setDescription(description);
        ReadableImage image = new ReadableImage();
        image.setImageUrl(String.format("https://cdn.example/%s.png", sku));
        product.setImage(image);
        if (SKU_VARIANT.equals(sku.value())) {
            ReadableVariantSelection selection = new ReadableVariantSelection();
            ReadableVariantOptionValue size = new ReadableVariantOptionValue();
            size.setOptionName("Size");
            size.setValueName("L");
            selection.setOptionValues(List.of(size));
            product.setVariant(selection);
        }
        return product;
    }

    private static SkuInventory stock(Sku sku) {
        return new SkuInventory(sku, 1L, true, !SKU_OUT.equals(sku.value()), 100, 1, 0,
                new SkuPrice(PRICE, PRICE, false, 0, null, null, null));
    }
}
