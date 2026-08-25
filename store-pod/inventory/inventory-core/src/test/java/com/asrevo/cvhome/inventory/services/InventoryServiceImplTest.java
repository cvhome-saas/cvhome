package com.asrevo.cvhome.inventory.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.entity.Inventory;
import com.asrevo.cvhome.inventory.entity.InventoryPrice;
import com.asrevo.cvhome.inventory.model.PersistableInventory;
import com.asrevo.cvhome.inventory.model.PersistablePrice;
import com.asrevo.cvhome.inventory.model.SkuInventory;
import com.asrevo.cvhome.inventory.repositories.InventoryRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The merchant's write side and the bulk read: an upsert creates or edits exactly one row per sku, null order limits
 * keep what is there, and a sku with several legacy rows always resolves to the first by id.
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("store-1");

    private static final String SKU = "SKU-1";

    private static final String SKU_2 = "SKU-2";

    private static final BigDecimal TEN = new BigDecimal("10");

    private static final BigDecimal EIGHT = new BigDecimal("8");

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryServiceImpl service;

    private static Inventory row(long id, String sku, int quantity) {
        Inventory inventory = new Inventory(STORE, sku);
        inventory.setId(id);
        inventory.setQuantity(quantity);
        return inventory;
    }

    private static PersistableInventory body(Integer min, Integer max, BigDecimal special) {
        return new PersistableInventory(9L, 12, true, min, max,
                new PersistablePrice(TEN, special, null, null));
    }

    @Test
    void emptySkuListNeverHitsTheDatabase() {
        assertThat(service.getBySkus(STORE, List.of())).isEmpty();
        verify(inventoryRepository, never()).findBySkus(any(), any());
    }

    @Test
    void firstRowByIdWinsWhenLegacyDataHoldsSeveralPerSku() {
        when(inventoryRepository.findBySkus(STORE, List.of(SKU, SKU_2)))
                .thenReturn(List.of(row(1, SKU, 5), row(2, SKU, 99), row(3, SKU_2, 1)));

        List<SkuInventory> result = service.getBySkus(STORE, List.of(SKU, SKU_2));

        assertThat(result).extracting(SkuInventory::sku).containsExactly(SKU, SKU_2);
        assertThat(result.getFirst().quantity()).isEqualTo(5);
        assertThat(result.getFirst().price()).isNull();
    }

    @Test
    void upsertCreatesTheRowAndItsDefaultPriceForANewSku() {
        when(inventoryRepository.findBySku(STORE, SKU)).thenReturn(Optional.empty());
        when(inventoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SkuInventory result = service.upsert(STORE, SKU, body(null, null, EIGHT));

        ArgumentCaptor<Inventory> captor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(captor.capture());
        Inventory saved = captor.getValue();
        assertThat(saved.getStoreMerchantId()).isEqualTo(STORE);
        assertThat(saved.getSku()).isEqualTo(SKU);
        assertThat(saved.getProductId()).isEqualTo(9L);
        assertThat(saved.getQuantity()).isEqualTo(12);
        assertThat(saved.getQuantityOrderMinimum()).isEqualTo(1);
        assertThat(saved.getQuantityOrderMaximum()).isZero();
        assertThat(saved.getPrices()).hasSize(1);
        InventoryPrice price = saved.getPrices().iterator().next();
        assertThat(price.isDefaultPrice()).isTrue();
        assertThat(price.getInventory()).isSameAs(saved);
        assertThat(price.getStoreMerchantId()).isEqualTo(STORE);
        assertThat(result.price().originalPrice()).isEqualByComparingTo(TEN);
        assertThat(result.price().finalPrice()).isEqualByComparingTo(EIGHT);
        assertThat(result.price().discounted()).isTrue();
        assertThat(result.canBePurchased()).isTrue();
    }

    @Test
    void upsertEditsTheExistingRowAndPriceInPlace() {
        Inventory existing = row(1, SKU, 1);
        existing.setQuantityOrderMinimum(2);
        existing.setQuantityOrderMaximum(6);
        InventoryPrice price = new InventoryPrice(existing);
        price.setAmount(BigDecimal.ONE);
        price.setSpecialAmount(BigDecimal.ONE);
        price.setSpecialStartDate(LocalDate.EPOCH);
        price.setSpecialEndDate(LocalDate.EPOCH);
        existing.getPrices().add(price);
        when(inventoryRepository.findBySku(STORE, SKU)).thenReturn(Optional.of(existing));
        when(inventoryRepository.save(existing)).thenReturn(existing);

        SkuInventory result = service.upsert(STORE, SKU, body(null, 4, null));

        assertThat(existing.getPrices()).hasSize(1);
        assertThat(price.getAmount()).isEqualByComparingTo(TEN);
        assertThat(price.getSpecialAmount()).isNull();
        assertThat(price.getSpecialStartDate()).isNull();
        assertThat(price.getSpecialEndDate()).isNull();
        assertThat(existing.getQuantityOrderMinimum()).as("null keeps the current value").isEqualTo(2);
        assertThat(existing.getQuantityOrderMaximum()).isEqualTo(4);
        assertThat(result.quantityOrderMinimum()).isEqualTo(2);
        assertThat(result.quantityOrderMaximum()).isEqualTo(4);
        assertThat(result.price().discounted()).isFalse();
    }

    @Test
    void upsertSetsBothOrderLimitsWhenGiven() {
        when(inventoryRepository.findBySku(STORE, SKU)).thenReturn(Optional.empty());
        when(inventoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SkuInventory result = service.upsert(STORE, SKU, body(3, 0, null));

        assertThat(result.quantityOrderMinimum()).isEqualTo(3);
        assertThat(result.quantityOrderMaximum()).isZero();
    }

    @Test
    void deleteByProductRemovesEveryRowOfTheProduct() {
        List<Inventory> rows = List.of(row(1, SKU, 1), row(2, SKU_2, 1));
        when(inventoryRepository.findByStoreMerchantIdAndProductId(STORE, 9L)).thenReturn(rows);

        service.deleteByProduct(STORE, 9L);

        verify(inventoryRepository).deleteAll(rows);
    }
}
