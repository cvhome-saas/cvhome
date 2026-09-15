package com.asrevo.cvhome.checkout.services.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import com.asrevo.cvhome.checkout.entity.Orders;
import com.asrevo.cvhome.commons.domain.Sku;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.model.AvailabilityQuery;
import com.asrevo.cvhome.inventory.model.SkuInventory;
import com.asrevo.cvhome.inventory.model.SkuPrice;
import com.asrevo.cvhome.inventory.services.ExternalInventoryService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Each sku is its own entry, so any cart's skus are answered from the entries that exist plus one read for the rest;
 * a sku inventory does not know is asked again; a forgotten sku is asked again; a store's entries are its own.
 */
@ExtendWith(MockitoExtension.class)
class CachedSkuInventoryTest {

    private static final StoreMerchantId OTHER = new StoreMerchantId("store-b");

    private static final Sku A = Sku.of("A");

    private static final Sku B = Sku.of("B");

    private static final Sku GONE = Sku.of("GONE");

    @Mock
    private ExternalInventoryService inventory;

    private CachedSkuInventory cached;

    private static SkuInventory stock(Sku sku) {
        return new SkuInventory(sku, 1L, true, true, 5, 1, 0,
                new SkuPrice(BigDecimal.TEN, BigDecimal.TEN, false, 0, null, null, null));
    }

    @BeforeEach
    void setUp() {
        cached = new CachedSkuInventory(new CaffeineCacheManager(CachedSkuInventory.CACHE), inventory);
        when(inventory.queryBySkus(any(), any())).thenAnswer(invocation -> {
            AvailabilityQuery query = invocation.getArgument(1);
            return query.skus().stream().filter(sku -> !GONE.equals(sku)).map(CachedSkuInventoryTest::stock).toList();
        });
    }

    @Test
    void aSkuIsReadOnceAndAnySubsetIsAnsweredFromTheEntries() {
        Map<Sku, SkuInventory> first = cached.stock(Orders.STORE, List.of(A, B, A, GONE));
        assertThat(first).containsOnlyKeys(A, B);
        assertThat(first.get(A).price().finalPrice()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(cached.stock(Orders.STORE, List.of(B))).containsOnlyKeys(B);
        assertThat(cached.stock(Orders.STORE, List.of(A, GONE))).containsOnlyKeys(A);

        // the first call read A, B and GONE; B alone hit; the third read only GONE again, which is never held
        verify(inventory).queryBySkus(Orders.STORE, new AvailabilityQuery(List.of(A, B, GONE)));
        verify(inventory).queryBySkus(Orders.STORE, new AvailabilityQuery(List.of(GONE)));
        verify(inventory, times(2)).queryBySkus(eq(Orders.STORE), any());
    }

    @Test
    void aForgottenSkuIsAskedAgainAndAnotherStoresEntriesAreItsOwn() {
        cached.stock(Orders.STORE, List.of(A, B));
        cached.forget(Orders.STORE, List.of(A));
        cached.forget(Orders.STORE, List.of());
        cached.forget(Orders.STORE, null);
        assertThat(cached.stock(Orders.STORE, List.of(A, B))).containsOnlyKeys(A, B);
        cached.stock(OTHER, List.of(A));
        assertThat(cached.stock(Orders.STORE, List.of())).isEmpty();
        assertThat(cached.stock(Orders.STORE, null)).isEmpty();

        verify(inventory).queryBySkus(Orders.STORE, new AvailabilityQuery(List.of(A, B)));
        verify(inventory).queryBySkus(Orders.STORE, new AvailabilityQuery(List.of(A)));
        verify(inventory).queryBySkus(OTHER, new AvailabilityQuery(List.of(A)));
        verify(inventory, never()).queryBySkus(any(), eq(new AvailabilityQuery(List.of())));
    }
}
