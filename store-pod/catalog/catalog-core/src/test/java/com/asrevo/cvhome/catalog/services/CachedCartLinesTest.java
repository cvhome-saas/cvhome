package com.asrevo.cvhome.catalog.services;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import com.asrevo.cvhome.catalog.model.product.ReadableCartLineProduct;
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
 * Each sku is its own cache entry, so any subset of a cart's skus hits, one read covers whatever is missing, a sku the
 * catalogue does not know is asked again next time, and a store's entries are its own.
 */
@ExtendWith(MockitoExtension.class)
class CachedCartLinesTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("store-a");

    private static final StoreMerchantId OTHER = new StoreMerchantId("store-b");

    private static final LanguageCode EN = LanguageCode.defaultLanguage();

    private static final Sku A = Sku.of("A");

    private static final Sku B = Sku.of("B");

    private static final Sku GONE = Sku.of("GONE");

    @Mock
    private ProductService products;

    private CachedCartLines lines;

    private static ReadableCartLineProduct line(Sku sku) {
        ReadableCartLineProduct line = new ReadableCartLineProduct();
        line.setSku(sku);
        line.setName(sku.value().toLowerCase());
        return line;
    }

    @BeforeEach
    void setUp() {
        lines = new CachedCartLines(new CaffeineCacheManager(CachedStorefrontCatalog.CART_LINE), products);
    }

    @Test
    void aSkuIsReadOnceAndAnySubsetOfSkusIsAnsweredFromTheEntries() {
        when(products.getCartLines(eq(STORE), any(), eq(EN))).thenAnswer(invocation -> {
            List<Sku> asked = invocation.getArgument(1);
            return asked.stream().filter(sku -> !GONE.equals(sku)).map(CachedCartLinesTest::line).toList();
        });

        assertThat(lines.cartLines(STORE, List.of(A, B, A, GONE), EN)).extracting(ReadableCartLineProduct::getSku)
                .containsExactly(A, B);
        assertThat(lines.cartLines(STORE, List.of(B), EN)).extracting(ReadableCartLineProduct::getSku)
                .containsExactly(B);
        assertThat(lines.cartLines(STORE, List.of(A, GONE), EN)).extracting(ReadableCartLineProduct::getSku)
                .containsExactly(A);

        // the first call read A and B; B alone hit; the third read only GONE again, which is never cached
        verify(products).getCartLines(STORE, List.of(A, B, GONE), EN);
        verify(products).getCartLines(STORE, List.of(GONE), EN);
        verify(products, times(2)).getCartLines(eq(STORE), any(), eq(EN));
    }

    @Test
    void anotherStoresEntriesAreItsOwnAndNothingIsAskedForNoSkus() {
        when(products.getCartLines(any(), any(), any())).thenAnswer(invocation -> {
            List<Sku> asked = invocation.getArgument(1);
            return asked.stream().map(CachedCartLinesTest::line).toList();
        });

        lines.cartLines(STORE, List.of(A), EN);
        lines.cartLines(OTHER, List.of(A), EN);
        assertThat(lines.cartLines(STORE, List.of(), EN)).isEmpty();
        assertThat(lines.cartLines(STORE, null, EN)).isEmpty();

        verify(products).getCartLines(STORE, List.of(A), EN);
        verify(products).getCartLines(OTHER, List.of(A), EN);
        verify(products, never()).getCartLines(any(), eq(List.of()), any());
    }
}
