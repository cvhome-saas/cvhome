package com.asrevo.cvhome.inventory.reads;

import java.math.BigDecimal;
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
import com.asrevo.cvhome.commons.domain.Sku;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.model.SkuInventory;
import com.asrevo.cvhome.inventory.model.SkuPrice;
import com.asrevo.cvhome.inventory.services.InventoryService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Each sku is its own entry: any subset hits, one read covers the rest, an unknown sku is asked again. */
@ExtendWith(MockitoExtension.class)
class SkuInventoryReadsTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("store-a");

    private static final StoreMerchantId OTHER = new StoreMerchantId("store-b");

    private static final Sku A = Sku.of("A");

    private static final Sku B = Sku.of("B");

    private static final Sku GONE = Sku.of("GONE");

    @Mock
    private InventoryService inventory;

    private SkuInventoryReads reads;

    private static SkuInventory stock(Sku sku) {
        return new SkuInventory(sku, 1L, true, true, 5, 1, 0,
                new SkuPrice(BigDecimal.TEN, BigDecimal.TEN, false, 0, null, null, null));
    }

    @BeforeEach
    void setUp() {
        CacheRegistry registry = new CacheRegistry(CacheRegions.of(InventoryRegions.values()),
                List.of(new CaffeineCacheProvider()), CacheProperties.defaults());
        reads = new SkuInventoryReads(registry, inventory);
        when(inventory.getBySkus(any(), any())).thenAnswer(invocation -> {
            List<Sku> asked = invocation.getArgument(1);
            return asked.stream().filter(sku -> !GONE.equals(sku)).map(SkuInventoryReadsTest::stock).toList();
        });
    }

    @Test
    void aSkuIsReadOnceAndAnySubsetIsAnsweredFromTheEntries() {
        assertThat(reads.bySkus(STORE, List.of(A, B, A, GONE))).extracting(SkuInventory::sku).containsExactly(A, B);
        assertThat(reads.bySkus(STORE, List.of(B))).extracting(SkuInventory::sku).containsExactly(B);
        assertThat(reads.bySkus(STORE, List.of(A, GONE))).extracting(SkuInventory::sku).containsExactly(A);
        assertThat(reads.bySkus(OTHER, List.of(A))).hasSize(1);
        assertThat(reads.bySkus(STORE, List.of())).isEmpty();
        assertThat(reads.bySkus(STORE, null)).isEmpty();

        verify(inventory).getBySkus(STORE, List.of(A, B, GONE));
        verify(inventory).getBySkus(STORE, List.of(GONE));
        verify(inventory).getBySkus(OTHER, List.of(A));
        verify(inventory, times(3)).getBySkus(any(), any());
        assertThat(InventoryRegions.SKU.regionName()).isEqualTo("inventory.sku");
        assertThat(InventoryRegions.SKU.ttl().toSeconds()).isEqualTo(5);
        assertThat(InventoryRegions.SKU.valueType()).isEqualTo(SkuInventory.class);
        assertThat(InventoryRegions.SKU.maxSize()).isEqualTo(50_000);
    }
}
