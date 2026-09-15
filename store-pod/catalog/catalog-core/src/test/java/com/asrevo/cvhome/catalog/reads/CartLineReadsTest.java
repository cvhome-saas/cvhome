package com.asrevo.cvhome.catalog.reads;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asrevo.cvhome.cache.CacheRegions;
import com.asrevo.cvhome.cache.CacheRegistry;
import com.asrevo.cvhome.cache.config.CacheProperties;
import com.asrevo.cvhome.cache.provider.caffeine.CaffeineCacheProvider;
import com.asrevo.cvhome.catalog.model.product.ReadableCartLineProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.Sku;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Each sku is its own entry, so any subset of a cart's skus hits, one read covers whatever is missing, a sku the
 * catalogue does not know is asked again next time, and a store's entries are its own.
 */
@ExtendWith(MockitoExtension.class)
class CartLineReadsTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("store-a");

    private static final StoreMerchantId OTHER = new StoreMerchantId("store-b");

    private static final LanguageCode EN = LanguageCode.defaultLanguage();

    private static final Sku A = Sku.of("A");

    private static final Sku B = Sku.of("B");

    private static final Sku GONE = Sku.of("GONE");

    @Mock
    private ProductService products;

    private CartLineReads reads;

    private static ReadableCartLineProduct line(Sku sku) {
        ReadableCartLineProduct line = new ReadableCartLineProduct();
        line.setSku(sku);
        return line;
    }

    private static ReadableMinimalProduct product(Sku sku) {
        ReadableMinimalProduct product = new ReadableMinimalProduct();
        product.setSku(sku);
        return product;
    }

    @BeforeEach
    void setUp() {
        CacheRegistry registry = new CacheRegistry(CacheRegions.of(CatalogRegions.values()),
                List.of(new CaffeineCacheProvider()), CacheProperties.defaults());
        reads = new CartLineReads(registry, products);
    }

    @Test
    void aSkuIsReadOnceAndAnySubsetOfSkusIsAnsweredFromTheEntries() {
        when(products.getCartLines(eq(STORE), any(), eq(EN))).thenAnswer(invocation -> {
            List<Sku> asked = invocation.getArgument(1);
            return asked.stream().filter(sku -> !GONE.equals(sku)).map(CartLineReadsTest::line).toList();
        });

        assertThat(reads.cartLines(STORE, EN, List.of(A, B, A, GONE))).extracting(ReadableCartLineProduct::getSku)
                .containsExactly(A, B);
        assertThat(reads.cartLines(STORE, EN, List.of(B))).extracting(ReadableCartLineProduct::getSku)
                .containsExactly(B);
        assertThat(reads.cartLines(STORE, EN, List.of(A, GONE))).extracting(ReadableCartLineProduct::getSku)
                .containsExactly(A);

        verify(products).getCartLines(STORE, List.of(A, B, GONE), EN);
        verify(products).getCartLines(STORE, List.of(GONE), EN);
        verify(products, times(2)).getCartLines(eq(STORE), any(), eq(EN));
    }

    @Test
    void theDetailedReadIsHeldTheSameWayAndAnotherStoresEntriesAreItsOwn() {
        when(products.getBySkus(any(), any(), any())).thenAnswer(invocation -> {
            List<Sku> asked = invocation.getArgument(1);
            return asked.stream().map(CartLineReadsTest::product).toList();
        });

        assertThat(reads.detailedProducts(STORE, EN, List.of(A))).extracting(ReadableMinimalProduct::getSku)
                .containsExactly(A);
        reads.detailedProducts(STORE, EN, List.of(A));
        reads.detailedProducts(OTHER, EN, List.of(A));
        assertThat(reads.detailedProducts(STORE, EN, List.of())).isEmpty();
        assertThat(reads.cartLines(STORE, EN, null)).isEmpty();

        verify(products).getBySkus(STORE, List.of(A), EN);
        verify(products).getBySkus(OTHER, List.of(A), EN);
        verify(products, never()).getCartLines(any(), any(), any());
    }
}
